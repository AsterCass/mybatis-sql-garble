package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import com.aster.plugin.garble.sql.SelectAuthFilterSqlCube;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author astercasc
 */
public class AuthenticationFilterSelectGarbleSql extends AuthenticationFilterSelectAbstract {


    public AuthenticationFilterSelectGarbleSql(
            Invocation invocation, AuthenticationFilterSelectProperty property) {
        super(invocation, property, property.getMethodForAuthCodeSelect());
    }


    @Override
    protected void exec() {
        String newSql = new SelectAuthFilterSqlCube(
                crossTableList, monitoredTableAuthColMap,
                monitoredTableAuthStrategyMap, monitoredTableAuthCodeMap)
                .addAuthCode(sql);
        newSqlBuilder(newSql);
    }



}
