package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterUpdateProperty;
import com.aster.plugin.garble.sql.UpdateAuthFilterSqlCube;
import com.aster.plugin.garble.sql.UpdateSqlCube;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Method;
import java.util.List;

public class AuthenticationFilterUpdateGarbleSql extends AuthenticationFilterUpdateAbstract {

    public AuthenticationFilterUpdateGarbleSql(
            Invocation invocation, AuthenticationFilterUpdateProperty property,
            List<Method> methodForAuthCodeSelect) {
        super(invocation, property, methodForAuthCodeSelect);
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
