package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterUpdateProperty;
import com.aster.plugin.garble.sql.UpdateAuthFilterSqlCube;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Invocation;

/**
 * @author astercasc
 */
@Slf4j
public class AuthenticationFilterUpdateGarbleSql extends AuthenticationFilterUpdateAbstract {

    public AuthenticationFilterUpdateGarbleSql(
            Invocation invocation, AuthenticationFilterUpdateProperty property) {
        super(invocation, property);
    }


    @Override
    protected void exec() {
        String newSql = new UpdateAuthFilterSqlCube(
                schema, crossGarbleTableSet,
                monitoredTableAuthColMap,
                monitoredTableAuthStrategyMap,
                monitoredTableAuthCodeMap).addAuthCode(sql);
        log.debug("[AuthenticationFilterUpdateGarbleSqlExe] origin sql: {}", sql);
        log.debug("[AuthenticationFilterUpdateGarbleSqlExe] new sql: {}", newSql);
        newSqlBuilder(newSql);
    }


}
