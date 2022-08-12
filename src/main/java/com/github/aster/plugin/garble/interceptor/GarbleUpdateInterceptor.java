package com.github.aster.plugin.garble.interceptor;

import com.github.aster.plugin.garble.work.MonitoredDataRollback;
import com.github.aster.plugin.garble.work.MonitoredUpdateSql;
import com.github.aster.plugin.garble.work.MonitoredWork;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


        Map<String, String> monitoredTableMap = new HashMap<String, String>() {{
            put("user", "id");
        }};

        if (invocation.getArgs()[0] instanceof MappedStatement) {
            MonitoredWork updateSql = new MonitoredUpdateSql(
                    invocation, "id", monitoredTableMap, new HashMap<>(), "dsafdsa");
            updateSql.run();
        }
        try {
            return invocation.proceed();
        } finally {
            MonitoredWork rollbackData = new MonitoredDataRollback(
                    invocation, "id", monitoredTableMap, new HashMap<>(), "dsafdsa");
            List<String> list = rollbackData.run();
            log.info(list.toString());
        }

    }


    @Override
    public Object plugin(Object target) {
        return (target instanceof Executor)
                ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties prop) {
        this.prop = prop;
    }


}
