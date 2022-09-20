package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationInsertProperty;
import com.aster.plugin.garble.sql.InsertAuthSqlCube;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author astercasc
 */
public class AuthenticationInsertGarbleSql extends AuthenticationInsertAbstract {


    public AuthenticationInsertGarbleSql(
            Invocation invocation, AuthenticationInsertProperty property) {
        super(invocation, property, property.getMethodForAuthCodeInsert());
    }


    @Override
    protected void exec() {
        String newSql = new InsertAuthSqlCube(
                crossTableList, monitoredTableAuthColMap, monitoredTableAuthCodeMap)
                .addAuthCode(sql);
        newSqlBuilder(newSql);
    }

}
