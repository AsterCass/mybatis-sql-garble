package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import com.aster.plugin.garble.sql.SelectSqlCube;
import org.apache.ibatis.plugin.Invocation;

/**
 * @author astercasc
 */
public abstract class AuthenticationFilterAbstract extends AuthenticationFilterSelectProperty {

    /**
     * builder
     */
    public AuthenticationFilterAbstract(
            Invocation invocation, AuthenticationFilterSelectProperty property) {


    }


    /**
     * 判断是否需要拦截
     */
    public void run() {
        if (notExcludedTableCondition(invocation, excludedMapperPath) &&
                (monitoredTableCondition(monitoredTableList, new SelectSqlCube()))) {
            exec();
        }
    }

    /**
     * execute
     */
    protected abstract void exec();


}
