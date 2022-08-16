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
                        Method method = propertyDto.getClass()
                                .getDeclaredMethod("set" + firstUpperName, String.class);
                        method.invoke(propertyDto, (String) prop.get(name));
                    }
                    if (type.equals("java.util.Map<java.lang.String, java.lang.String>")) {
                        Method method = propertyDto.getClass()
                                .getDeclaredMethod("set" + firstUpperName, Map.class);
                        String mapStr;
                        //这里if判断是兼容mybatis-config和yml文件的配置，mybatis的配置value值只能输入String格式，下同
                        if(prop.get(name) instanceof String) {
                            mapStr = prop.get(name).toString();
                        } else {
                            mapStr = JSON.toJSONString(prop.get(name));
                        }
                        Map strMap = JSON.parseObject(mapStr, Map.class);
                        method.invoke(propertyDto, strMap);
                    }

                    if (type.equals("java.util.List<java.lang.String>")) {
                        Method method = propertyDto.getClass()
                                .getDeclaredMethod("set" + firstUpperName, List.class);
                        String listStr;
                        if(prop.get(name) instanceof String) {
                            listStr = prop.get(name).toString();
                        } else {
                            listStr = JSON.toJSONString(prop.get(name));
                        }
                        List<String> strList = JSON.parseArray(listStr, String.class);
                        method.invoke(propertyDto, strList);
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
