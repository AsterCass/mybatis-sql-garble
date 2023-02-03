package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationInsertProperty;
import com.aster.plugin.garble.sql.InsertAuthSqlCube;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Invocation;

/**
 * @author astercasc
 */
@Slf4j
public class AuthenticationInsertGarbleSql extends AuthenticationInsertAbstract {


    public AuthenticationInsertGarbleSql(
            Invocation invocation, AuthenticationInsertProperty property) {
        super(invocation, property);
    }


    @Override
    protected void exec() {
        String newSql = new InsertAuthSqlCube(schema,
                crossGarbleTableSet, monitoredTableAuthColMap, monitoredTableAuthCodeMap)
                .addAuthCode(sql);
        log.debug("[AuthenticationInsertGarbleSql] origin sql: {}", sql);
        log.debug("[AuthenticationInsertGarbleSql] new sql: {}", newSql);
        newSqlBuilder(newSql);
    }

}
