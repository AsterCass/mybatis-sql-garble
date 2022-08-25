package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author astercasc
 */
public class AuthenticationFilterGarbleSql extends AuthenticationFilterAbstract {


    public AuthenticationFilterGarbleSql(
            Invocation invocation, AuthenticationFilterSelectProperty property,
            List<Method> methodForAuthCodeSelect) {
        super(invocation, property, methodForAuthCodeSelect);
    }


    @Override
    protected void exec(String authCode) {






    }
}
