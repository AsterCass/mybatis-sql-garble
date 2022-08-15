package com.github.aster.plugin.garble.interceptor;

import com.github.aster.plugin.garble.dto.PropertyDto;
import com.github.aster.plugin.garble.service.DealWithUpdatedService;
import com.github.aster.plugin.garble.work.MonitoredDataRollback;
import com.github.aster.plugin.garble.work.MonitoredUpdateSql;
import com.github.aster.plugin.garble.work.MonitoredWork;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class GarbleUpdateInterceptor implements Interceptor {

    private PropertyDto prop;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {


        Map<String, String> monitoredTableMap = new HashMap<String, String>() {{
            put("user", "id");
            put("hr_room", "id");
            put("hr_house_pr", "id");
        }};

        Map<String, String> monitoredTableUpdateFlagColMap = new HashMap<String, String>() {{
            put("user", "update_record");
            put("hr_room", "update_record");
            put("hr_house_pr", "update_record");
        }};

        List<String> excludedMapperPath = new ArrayList<>();

        String defaultFlagColName = "update_record";


        if (invocation.getArgs()[0] instanceof MappedStatement) {
            MonitoredWork updateSql = new MonitoredUpdateSql(
                    invocation, defaultFlagColName, monitoredTableMap, monitoredTableUpdateFlagColMap, excludedMapperPath);
            updateSql.run();
        }
        try {
            return invocation.proceed();
        } finally {
            MonitoredWork rollbackData = new MonitoredDataRollback(
                    invocation, defaultFlagColName, monitoredTableMap, monitoredTableUpdateFlagColMap, excludedMapperPath);
            Map<String, List<String>> list = rollbackData.run();
            log.info(list.toString());
            DealWithUpdatedService.load();
        }

    }


    @Override
    public Object plugin(Object target) {
        return (target instanceof Executor)
                ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties prop) {
        if(null != prop && 0 != prop.size()) {
            Field[] declaredFields  = PropertyDto.class.getDeclaredFields();
            for (Field property : declaredFields) {

            }
            System.out.println(declaredFields);
        }
    }


}
