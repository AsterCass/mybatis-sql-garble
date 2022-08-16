package com.github.aster.plugin.garble.dto;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Data
public class PropertyDto {


    /**
     * 监控表和监控返回字段的Map，一般为主键，("user", "id")
     */
    Map<String, String> monitoredTableMap;

    /**
     * 监控表和更新标记字段Map ("user", "update_record")
     */
    Map<String, String> monitoredTableUpdateFlagColMap;

    /**
     * 默认更新标记字段，如果监控表无法在更新标记字段Map中取得，则会使用默认更新标记字段
     */
    String defaultFlagColName;

    /**
     * 不拦截的sql的路径
     */
    List<String> excludedMapperPath;


    public static PropertyDto build(Properties prop) {
        PropertyDto propertyDto = new PropertyDto();
        if (null != prop && 0 != prop.size()) {
            Field[] declaredFields = PropertyDto.class.getDeclaredFields();
            for (Field property : declaredFields) {
                String name = property.getName();
                String type = property.getGenericType().toString();
                property.setAccessible(true);
                String firstUpperName = firstUpperCase(name);
                try {
                    if (type.equals("class java.lang.String")) {
                        Method Method = propertyDto.getClass()
                                .getDeclaredMethod("set" + firstUpperName, String.class);
                        Method.invoke(propertyDto, (String) prop.get(name));
                    }

                    if (type.equals("java.util.Map<java.lang.String, java.lang.String>")) {
                        Method Method = propertyDto.getClass()
                                .getDeclaredMethod("set" + firstUpperName, Map.class);
                        Map strMap = JSON.parseObject(prop.get(name).toString(), Map.class);
                        Method.invoke(propertyDto, strMap);
                    }

                    if (type.equals("java.util.List<java.lang.String>")) {
                        Method Method = propertyDto.getClass()
                                .getDeclaredMethod("set" + firstUpperName, List.class);
                        List<String> strList = JSON.parseArray(prop.get(name).toString(), String.class);
                        Method.invoke(propertyDto, strList);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return propertyDto;
    }

    private static String firstUpperCase(String str) {
        return str.replaceFirst(str.substring(0, 1), str.substring(0, 1).toUpperCase());
    }


}
