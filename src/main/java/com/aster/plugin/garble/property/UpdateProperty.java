package com.aster.plugin.garble.property;

import com.alibaba.fastjson.JSON;
import com.aster.plugin.garble.enums.GarbleFunctionEnum;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author astercasc
 */
@Data
public class UpdateProperty {

    /**
     * 标记实现DealWithUpdatedInterface接口的方法路径，加快加快初始化速度，可以不赋值
     */
    private String dealWithUpdatedPath;

    /**
     * 监控表和监控返回字段的Map，一般为主键，("user", "id")
     */
    private Map<String, String> monitoredTableMap;

    /**
     * 监控表和更新标记字段Map ("user", "update_record")
     */
    private Map<String, String> monitoredTableUpdateFlagColMap;

    /**
     * 默认更新标记字段，如果监控表无法在更新标记字段Map中取得，则会使用默认更新标记字段
     */
    private String defaultFlagColName;

    /**
     * 不拦截的sql的路径
     */
    private List<String> excludedMapperPath;

    /**
     * 需要的功能
     * {@link GarbleFunctionEnum}
     */
    private List<Integer> garbleFunctionList;


    public static UpdateProperty build(Properties prop) {
        UpdateProperty updateProperty = new UpdateProperty();
        if (null != prop && 0 != prop.size()) {
            Field[] declaredFields = UpdateProperty.class.getDeclaredFields();
            for (Field property : declaredFields) {
                String name = property.getName();
                String type = property.getGenericType().toString();
                property.setAccessible(true);
                String firstUpperName = firstUpperCase(name);
                try {
                    if ("class java.lang.String".equals(type)) {
                        Method method = updateProperty.getClass()
                                .getDeclaredMethod("set" + firstUpperName, String.class);
                        method.invoke(updateProperty, (String) prop.get(name));
                    }
                    if ("java.util.Map<java.lang.String, java.lang.String>".equals(type)) {
                        Method method = updateProperty.getClass()
                                .getDeclaredMethod("set" + firstUpperName, Map.class);
                        String mapStr;
                        //这里if判断是兼容mybatis-config和yml文件的配置，mybatis的配置value值只能输入String格式，下同
                        if(prop.get(name) instanceof String) {
                            mapStr = prop.get(name).toString();
                        } else {
                            mapStr = JSON.toJSONString(prop.get(name));
                        }
                        Map strMap = JSON.parseObject(mapStr, Map.class);
                        method.invoke(updateProperty, strMap);
                    }

                    if ("java.util.List<java.lang.String>".equals(type)) {
                        Method method = updateProperty.getClass()
                                .getDeclaredMethod("set" + firstUpperName, List.class);
                        String listStr;
                        if(prop.get(name) instanceof String) {
                            listStr = prop.get(name).toString();
                        } else {
                            listStr = JSON.toJSONString(prop.get(name));
                        }
                        List<String> strList = JSON.parseArray(listStr, String.class);
                        method.invoke(updateProperty, strList);
                    }

                    if ("java.util.List<java.lang.Integer>".equals(type)) {
                        Method method = updateProperty.getClass()
                                .getDeclaredMethod("set" + firstUpperName, List.class);
                        String listStr;
                        if(prop.get(name) instanceof String) {
                            listStr = prop.get(name).toString();
                        } else {
                            listStr = JSON.toJSONString(prop.get(name));
                        }
                        List<Integer> strList = JSON.parseArray(listStr, Integer.class);
                        method.invoke(updateProperty, strList);
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return updateProperty;
    }

    private static String firstUpperCase(String str) {
        return str.replaceFirst(str.substring(0, 1), str.substring(0, 1).toUpperCase());
    }


}
