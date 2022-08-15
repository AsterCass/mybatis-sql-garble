package com.github.aster.plugin.garble.interceptor;

import com.alibaba.fastjson.JSON;
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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class GarbleUpdateInterceptor implements Interceptor {

    private PropertyDto prop;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {


//        Map<String, String> monitoredTableMap = new HashMap<String, String>() {{
//            put("user", "id");
//            put("hr_room", "id");
//            put("hr_house_pr", "id");
//        }};
//
//        Map<String, String> monitoredTableUpdateFlagColMap = new HashMap<String, String>() {{
//            put("user", "update_record");
//            put("hr_room", "update_record");
//            put("hr_house_pr", "update_record");
//        }};
//
//        List<String> excludedMapperPath = new ArrayList<>();
//
//        String defaultFlagColName = "update_record";


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
        PropertyDto propertyDto = new PropertyDto();
        if(null != prop && 0 != prop.size()) {
            Field[] declaredFields  = PropertyDto.class.getDeclaredFields();
            for (Field property : declaredFields) {
                String name = property.getName();
                String type = property.getGenericType().toString();
                property.setAccessible(true);

                String firstUpperName = firstUpperCase(name);
                try {
                    if (type.equals("class java.lang.String")) {
                        Method Method = propertyDto.getClass().getDeclaredMethod("set" + firstUpperName, String.class);
                        Method.invoke(propertyDto, (String) prop.get(name));
                    }

                    if (type.equals("java.util.Map<java.lang.String, java.lang.String>")) {
                        Method Method = propertyDto.getClass().getDeclaredMethod("set" + firstUpperName, Map.class);
                        Map strMap = JSON.parseObject(prop.get(name).toString(), Map.class);
                        Method.invoke(propertyDto, strMap);
                    }

                    if (type.equals("java.util.List<java.lang.String>")) {
                        Method Method = propertyDto.getClass().getDeclaredMethod("set" + firstUpperName, List.class);
                        List<String> strList = JSON.parseArray(prop.get(name).toString(), String.class);
                        Method.invoke(propertyDto, strList);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        this.prop = propertyDto;
    }

    private String firstUpperCase(String str) {
        return str.replaceFirst(str.substring(0, 1), str.substring(0, 1).toUpperCase());
    }


}
