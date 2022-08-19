package com.aster.plugin.garble.interceptor;

import com.aster.plugin.garble.enums.GarbleFunctionEnum;
import com.aster.plugin.garble.property.UpdateProperty;
import com.aster.plugin.garble.service.DealWithUpdated;
import com.aster.plugin.garble.service.DealWithUpdatedService;
import com.aster.plugin.garble.work.MonitoredDataRollback;
import com.aster.plugin.garble.work.MonitoredUpdateSql;
import com.aster.plugin.garble.work.MonitoredWork;
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
    private UpdateProperty prop;

    /**
     * 继承 DealWithUpdatedInterface 的方法，用于做返回更新行的后续处理
     */
    private List<Method> postMethodForUpdatedRows;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (invocation.getArgs()[0] instanceof MappedStatement) {
            //更改更新sql 获取更新行
            if (null != prop.getGarbleFunctionList() &&
                    prop.getGarbleFunctionList().contains(GarbleFunctionEnum.GET_UPDATED_DATA.getCode())) {
                MonitoredWork updateSql = new MonitoredUpdateSql(
                        invocation, prop.getDefaultFlagColName(),
                        prop.getMonitoredTableMap(), prop.getMonitoredTableUpdateFlagColMap(),
                        prop.getExcludedMapperPath());
                updateSql.run();
            }

        }
        try {
            return invocation.proceed();
        } finally {
            //获取更新行
            if (null != prop.getGarbleFunctionList() &&
                    prop.getGarbleFunctionList().contains(GarbleFunctionEnum.GET_UPDATED_DATA.getCode())) {
                MonitoredWork rollbackData = new MonitoredDataRollback(
                        invocation, prop.getDefaultFlagColName(),
                        prop.getMonitoredTableMap(), prop.getMonitoredTableUpdateFlagColMap(),
                        prop.getExcludedMapperPath());
                Map<String, List<String>> list = rollbackData.run();
                //后续操作
                List<Method> sortedMethodList =
                        postMethodForUpdatedRows.stream().sorted(Comparator.comparing(
                                method -> method.getAnnotation(DealWithUpdated.class)
                                        .priority())).collect(Collectors.toList());
                if (0 != postMethodForUpdatedRows.size()) {
                    for (Method method : sortedMethodList) {
                        method.invoke(method.getDeclaringClass().newInstance(), list);
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
    public void setProperties(Properties prop) {
        this.prop = UpdateProperty.build(prop);
        this.postMethodForUpdatedRows = DealWithUpdatedService
                .loadBySubTypes(this.prop.getDealWithUpdatedPath());
    }


}
