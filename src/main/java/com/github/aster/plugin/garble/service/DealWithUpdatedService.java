package com.github.aster.plugin.garble.service;


import org.reflections.Reflections;
import org.reflections.scanners.*;
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
        Set<Method> methods =  reflections.getMethodsAnnotatedWith(DealWithUpdated.class);
        return new ArrayList<>(methods);
    }
}
