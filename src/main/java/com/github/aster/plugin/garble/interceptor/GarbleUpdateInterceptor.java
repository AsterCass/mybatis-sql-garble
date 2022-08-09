package com.github.aster.plugin.garble.interceptor;

import com.github.aster.plugin.garble.work.MonitoredDataRollback;
import com.github.aster.plugin.garble.work.MonitoredUpdateSql;
import com.github.aster.plugin.garble.work.MonitoredWork;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.util.Arrays;
import java.util.Properties;

@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class GarbleUpdateInterceptor implements Interceptor {

    private Properties prop;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {


        MonitoredWork updateSql = new MonitoredUpdateSql(invocation,
                "id", Arrays.asList("user"), "dsafdsa");
        updateSql.run();

        try {
            return invocation.proceed();
        } finally {
            if (invocation.getArgs()[0] instanceof MappedStatement) {
                MonitoredWork rollbackData = new MonitoredDataRollback(invocation,
                        "id", Arrays.asList("user"), "dsafdsa");
                String listJson = rollbackData.run();
                log.info(listJson);
            }
        }


    }


    @Override
    public Object plugin(Object target) {
        return (target instanceof Executor || target instanceof StatementHandler)
                ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties prop) {
        this.prop = prop;
    }


}