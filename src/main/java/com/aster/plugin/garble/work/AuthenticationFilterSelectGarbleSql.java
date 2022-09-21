package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import com.aster.plugin.garble.sql.SelectAuthFilterSqlCube;

/**
 * @author astercasc
 */
public class AuthenticationFilterSelectGarbleSql extends AuthenticationFilterSelectAbstract {


    public AuthenticationFilterSelectGarbleSql(AuthenticationFilterSelectProperty property) {
        super(property.getInvocation(), property, property.getMethodForAuthCodeSelect());
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
