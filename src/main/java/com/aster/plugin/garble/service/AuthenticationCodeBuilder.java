package com.aster.plugin.garble.service;

import com.aster.plugin.garble.enums.AuthenticationStrategyEnum;
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
     * {@link AuthenticationStrategyEnum}
     */
    int function();

    /**
     * {@link AuthenticationTypeEnum}
     */
    int type();

}
