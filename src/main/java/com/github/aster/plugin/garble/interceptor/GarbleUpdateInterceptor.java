package com.github.aster.plugin.garble.interceptor;

import com.github.aster.plugin.garble.dto.PropertyDto;
import com.github.aster.plugin.garble.service.DealWithUpdated;
import com.github.aster.plugin.garble.service.DealWithUpdatedService;
import com.github.aster.plugin.garble.work.MonitoredDataRollback;
import com.github.aster.plugin.garble.work.MonitoredUpdateSql;
import com.github.aster.plugin.garble.work.MonitoredWork;
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

@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class GarbleUpdateInterceptor implements Interceptor {

    private PropertyDto prop;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (invocation.getArgs()[0] instanceof MappedStatement) {
            MonitoredWork updateSql = new MonitoredUpdateSql(
                    invocation, prop.getDefaultFlagColName(),
                    prop.getMonitoredTableMap(), prop.getMonitoredTableUpdateFlagColMap(),
                    prop.getExcludedMapperPath());
            updateSql.run();
        }
        try {
            return invocation.proceed();
        } finally {
            MonitoredWork rollbackData = new MonitoredDataRollback(
                    invocation, prop.getDefaultFlagColName(),
                    prop.getMonitoredTableMap(), prop.getMonitoredTableUpdateFlagColMap(),
                    prop.getExcludedMapperPath());
            Map<String, List<String>> list = rollbackData.run();
            List<Method> methods = DealWithUpdatedService.load();
            List<Method> sortedMethodList =
                    methods.stream().sorted(Comparator.comparing(method -> method.getAnnotation(DealWithUpdated.class)
                            .priority())).collect(Collectors.toList());
            if (0 != methods.size()) {
                for (Method method : sortedMethodList) {
                    method.invoke(method.getDeclaringClass().newInstance(), list);
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
        this.prop = PropertyDto.build(prop);
    }


}
