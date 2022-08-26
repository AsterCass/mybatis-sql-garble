package com.aster.plugin.garble.service;

import com.aster.plugin.garble.enums.AuthenticationTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author astercasc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AuthenticationCodeBuilder {

    /**
     * {@link AuthenticationTypeEnum}
     */
    int type();


    /**
     * 支持的table，在配置文件中配置的监控表，每一个都需要在注解中标明如何获取对应的code
     */
    String[] tables();

}
