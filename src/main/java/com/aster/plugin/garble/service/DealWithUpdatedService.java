package com.aster.plugin.garble.service;


import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author astercasc
 */
public class DealWithUpdatedService {

    @Deprecated
    public static List<Method> load(String path) {
        //Reflections 工具的使用可以参照 https://www.baeldung.com/reflections-library
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(path)
                .addScanners(Scanners.MethodsAnnotated)
        );
        //这里可以优化一下，这样遍历所用方法确实太慢了
        Set<Method> methodAnn = reflections.getMethodsAnnotatedWith(DealWithUpdated.class);
        List<Method> methodList = new ArrayList<>();
        if (null != methodAnn && 0 != methodAnn.size()) {
            for (Method method : methodAnn) {
                //名称为 execute 且继承了 DealWithUpdatedInterface
                if (method.getName().equals(DealWithUpdatedInterface.class.getMethods()[0].getName()) &&
                        DealWithUpdatedInterface.class.isAssignableFrom(method.getDeclaringClass())) {
                    methodList.add(method);
                }
            }
        }
        return methodList;
    }

    public static List<Method> loadBySubTypes(String path) {
        path = null == path ? "" : path;
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(path)
                .addScanners(Scanners.SubTypes)
        );
        Set<Class<? extends DealWithUpdatedInterface>> classSet =
                reflections.getSubTypesOf(DealWithUpdatedInterface.class);
        List<Method> methodList = new ArrayList<>();
        if(null != classSet && 0 != classSet.size()) {
            for (Class<? extends DealWithUpdatedInterface> dealClass : classSet) {
                Method[] methods = dealClass.getMethods();
                for (Method method : methods) {
                    //名称为 execute 且 包含 DealWithUpdated 注解
                    if (method.getName().equals(DealWithUpdatedInterface.class.getMethods()[0].getName())
                        && Arrays.stream(method.getDeclaredAnnotations())
                            .anyMatch(ann -> ann instanceof DealWithUpdated)) {
                        methodList.add(method);
                    }
                }
            }
        }
        return methodList;

    }

}
