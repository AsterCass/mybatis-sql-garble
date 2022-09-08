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
            Invocation invocation, AuthenticationFilterSelectProperty property,
            List<Method> methodForAuthCodeSelect) {
        super(invocation, property, methodForAuthCodeSelect);
    }


    @Override
    protected void exec() {
        String newSql = new SelectAuthFilterSqlCube(
                monitoredTableList, monitoredTableAuthColMap,
                monitoredTableAuthStrategyMap, monitoredTableAuthCodeMap)
                .addAuthCode(sql);
        newSqlBuilder(newSql);
    }



}
