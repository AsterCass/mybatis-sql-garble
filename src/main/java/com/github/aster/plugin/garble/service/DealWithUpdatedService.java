package com.github.aster.plugin.garble.service;


import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DealWithUpdatedService {

    public static List<Method> load() {
        //Reflections 工具的使用可以参照 https://www.baeldung.com/reflections-library
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages("")
                .addScanners(Scanners.MethodsAnnotated)
        );

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
}
