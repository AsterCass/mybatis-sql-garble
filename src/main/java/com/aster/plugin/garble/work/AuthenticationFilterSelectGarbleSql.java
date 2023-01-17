package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import com.aster.plugin.garble.sql.SelectAuthFilterSqlCube;
import lombok.extern.slf4j.Slf4j;

/**
 * @author astercasc
 */
@Slf4j
public class AuthenticationFilterSelectGarbleSql extends AuthenticationFilterSelectAbstract {


    public AuthenticationFilterSelectGarbleSql(AuthenticationFilterSelectProperty property) {
        super(property.getInvocation(), property, property.getMethodForAuthCodeSelect());
    }


    @Override
    protected void exec() {
        String newSql = new SelectAuthFilterSqlCube(
                schema, crossGarbleTableSet, monitoredTableAuthColMap,
                monitoredTableAuthStrategyMap, monitoredTableAuthCodeMap)
                .addAuthCode(sql);
        log.debug("[AuthenticationFilterSelectGarbleSqlExe] origin sql: {}", sql);
        log.debug("[AuthenticationFilterSelectGarbleSqlExe] new sql: {}", newSql);
        newSqlBuilder(newSql);
    }



}
