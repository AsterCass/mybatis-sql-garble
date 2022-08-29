package com.aster.plugin.garble.interceptor;

import com.aster.plugin.garble.enums.GarbleFunctionEnum;
import com.aster.plugin.garble.property.UpdatedDataMsgProperty;
import com.aster.plugin.garble.service.DealWithUpdated;
import com.aster.plugin.garble.service.SpecifiedMethodGenerator;
import com.aster.plugin.garble.util.PropertyUtil;
import com.aster.plugin.garble.work.UpdatedDataMsgAbstract;
import com.aster.plugin.garble.work.UpdatedDataMsgGarbleSql;
import com.aster.plugin.garble.work.UpdatedDataMsgGetUpdated;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 功能：
 * 1. 获取监控表的更新行，支持后续操作（非异步）
 * @author astercasc
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class GarbleUpdateInterceptor implements Interceptor {

    /**
     * 传入配置
     */
    private UpdatedDataMsgProperty updatedDataMsgProperty;

    /**
     * 继承 DealWithUpdatedInterface 的方法，用于做返回更新行的后续处理
     */
    private List<Method> postMethodForUpdatedRows;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (invocation.getArgs()[0] instanceof MappedStatement) {
            //更改更新sql 获取更新行
            if (null != updatedDataMsgProperty) {
                UpdatedDataMsgAbstract garbleSql = new UpdatedDataMsgGarbleSql(
                        invocation, updatedDataMsgProperty);
                garbleSql.run();
            }

        }
        try {
            return invocation.proceed();
        } finally {
            //获取更新行
            if (null != updatedDataMsgProperty) {
                UpdatedDataMsgAbstract rollbackData = new UpdatedDataMsgGetUpdated(
                        invocation, updatedDataMsgProperty);
                Map<String, List<String>> list = rollbackData.run();
                //后续操作
                if (null != list && 0 != list.size()) {
                    List<Method> sortedMethodList =
                            postMethodForUpdatedRows.stream().sorted(Comparator.comparing(
                                    method -> method.getAnnotation(DealWithUpdated.class)
                                            .priority())).collect(Collectors.toList());
                    if (0 != postMethodForUpdatedRows.size()) {
                        for (Method method : sortedMethodList) {
                            method.invoke(method.getDeclaringClass().getDeclaredConstructor().newInstance(), list);
                        }
                    }
                }
            }
        }

    }


    @Override
    public Object plugin(Object target) {
        return (target instanceof Executor)
                ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties properties) {
        //这里是兼容maven配置调用, spring boot 直接会调用 setAuthenticationFilterSelectProperty 方法
        if (null != properties) {
            Properties updatedDataMsgProperty = new Properties();

            for (Object key : properties.keySet()) {
                if (key instanceof String &&
                        ((String) key).contains(GarbleFunctionEnum.UPDATED_DATA_MSG.getPropertyPre())) {
                    updatedDataMsgProperty.put(
                            ((String) key).replace(GarbleFunctionEnum.UPDATED_DATA_MSG.getPropertyPre(),
                                    ""),
                            properties.get(key));
                }

            }
            if (0 != updatedDataMsgProperty.size()) {
                setUpdatedDataMsgProperty(updatedDataMsgProperty);
            }

        }
    }

    /**
     * 设置更新数据回调的相关属性
     */
    public void setUpdatedDataMsgProperty(Properties prop) {
        this.updatedDataMsgProperty = PropertyUtil.propertyToObject(prop, UpdatedDataMsgProperty.class);
        if (null != updatedDataMsgProperty) {
            this.postMethodForUpdatedRows = SpecifiedMethodGenerator
                    .loadUpdatedMsgBySubTypes(this.updatedDataMsgProperty.getDealWithUpdatedPath());
        }
    }


}
