package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterUpdateProperty;
import com.aster.plugin.garble.sql.UpdateAuthFilterSqlCube;
import org.apache.ibatis.plugin.Invocation;

public class AuthenticationFilterUpdateGarbleSql extends AuthenticationFilterUpdateAbstract {

    public AuthenticationFilterUpdateGarbleSql(
            Invocation invocation, AuthenticationFilterUpdateProperty property) {
        super(invocation, property, property.getMethodForAuthCodeUpdate());
    }


    @Override
    protected void exec() {
        String newSql = new UpdateAuthFilterSqlCube(crossTableList,
                monitoredTableAuthColMap,
                monitoredTableAuthStrategyMap,
                monitoredTableAuthCodeMap).addAuthCode(sql);
        newSqlBuilder(newSql);
    }


}
