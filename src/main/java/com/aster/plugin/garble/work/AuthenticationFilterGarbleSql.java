package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import org.apache.ibatis.plugin.Invocation;

/**
 * @author astercasc
 */
public class AuthenticationFilterGarbleSql extends AuthenticationFilterAbstract {


    public AuthenticationFilterGarbleSql(Invocation invocation, AuthenticationFilterSelectProperty property) {
        super(invocation, property);
    }


    @Override
    protected void exec() {
        return;
    }
}
