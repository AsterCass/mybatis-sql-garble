package com.aster.plugin.garble.interceptor;

import com.aster.plugin.garble.enums.AuthenticationTypeEnum;
import com.aster.plugin.garble.enums.GarbleFunctionEnum;
import com.aster.plugin.garble.property.AuthenticationFilterUpdateProperty;
import com.aster.plugin.garble.property.AuthenticationInsertProperty;
import com.aster.plugin.garble.property.UpdatedDataMsgProperty;
import com.aster.plugin.garble.service.DealWithUpdated;
import com.aster.plugin.garble.service.SpecifiedMethodGenerator;
import com.aster.plugin.garble.util.PropertyUtil;
import com.aster.plugin.garble.work.*;
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
 *
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
     * 传入配置
     */
    private AuthenticationInsertProperty insertAuthProperty;

    /**
     * 传入配置
     */
    private AuthenticationFilterUpdateProperty updateAuthProperty;

    /**
     * 继承 DealWithUpdatedInterface 的方法，用于做返回更新行的后续处理
     */
    private List<Method> postMethodForUpdatedRows;

    /**
     * 继承 AuthenticationCodeInterface 用于获取鉴权code的方法，
     */
    private List<Method> methodForAuthCodeInsert;

    /**
     * 继承 AuthenticationCodeInterface 用于获取鉴权code的方法，
     */
    private List<Method> methodForAuthCodeUpdate;


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

        if (invocation.getArgs()[0] instanceof MappedStatement) {
            if (null != insertAuthProperty) {
                AuthenticationInsertAbstract garbleSql = new AuthenticationInsertGarbleSql(
                        invocation, insertAuthProperty, methodForAuthCodeInsert);
                garbleSql.run();
            }
        }

        if (invocation.getArgs()[0] instanceof MappedStatement) {
            if (null != updateAuthProperty) {
                AuthenticationFilterUpdateAbstract garbleSql = new AuthenticationFilterUpdateGarbleSql(
                        invocation, updateAuthProperty, methodForAuthCodeUpdate);
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
            Properties insertAuthProperty = new Properties();
            Properties updateAuthProperty = new Properties();


            for (Object key : properties.keySet()) {
                if (key instanceof String &&
                        ((String) key).contains(GarbleFunctionEnum.UPDATED_DATA_MSG.getPropertyPre())) {
                    updatedDataMsgProperty.put(
                            ((String) key).replace(GarbleFunctionEnum.UPDATED_DATA_MSG.getPropertyPre(),
                                    ""),
                            properties.get(key));
                }
                if (key instanceof String &&
                        ((String) key).contains(GarbleFunctionEnum.INSERT_AUTHENTICATION.getPropertyPre())) {
                    insertAuthProperty.put(
                            ((String) key).replace(GarbleFunctionEnum.INSERT_AUTHENTICATION.getPropertyPre(),
                                    ""),
                            properties.get(key));
                }
                if (key instanceof String &&
                        ((String) key).contains(GarbleFunctionEnum.UPDATE_AUTHENTICATION.getPropertyPre())) {
                    updateAuthProperty.put(
                            ((String) key).replace(GarbleFunctionEnum.UPDATE_AUTHENTICATION.getPropertyPre(),
                                    ""),
                            properties.get(key));
                }
            }
            if (0 != updatedDataMsgProperty.size()) {
                setUpdatedDataMsgProperty(updatedDataMsgProperty);
            }
            if (0 != insertAuthProperty.size()) {
                setInsertAuthProperty(insertAuthProperty);
            }
            if (0 != updateAuthProperty.size()) {
                setUpdateAuthProperty(updateAuthProperty);
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

    /**
     * 设置插入授權相关属性
     */
    public void setInsertAuthProperty(Properties prop) {
        this.insertAuthProperty = PropertyUtil.propertyToObject(prop, AuthenticationInsertProperty.class);
        if (null != insertAuthProperty) {
            this.methodForAuthCodeInsert = SpecifiedMethodGenerator.loadAuthCodeBySubTypes(
                    this.insertAuthProperty.getAuthCodePath(),
                    AuthenticationTypeEnum.INSERT
            );
        }
    }

    /**
     * 设置更新鉴权相关属性
     */
    public void setUpdateAuthProperty(Properties prop) {
        this.updateAuthProperty = PropertyUtil.propertyToObject(prop, AuthenticationFilterUpdateProperty.class);
        if (null != updateAuthProperty) {
            this.methodForAuthCodeUpdate = SpecifiedMethodGenerator.loadAuthCodeBySubTypes(
                    this.updateAuthProperty.getAuthCodePath(),
                    AuthenticationTypeEnum.UPDATE
            );
        }
    }


}
