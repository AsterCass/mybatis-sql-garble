package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import org.apache.ibatis.plugin.Invocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public abstract class AuthenticationFilterAbstract extends AuthenticationFilterSelectProperty {

    /**
     * builder
     */
    public AuthenticationFilterAbstract(Invocation invocation, AuthenticationFilterSelectProperty property) {


    }


    /**
     * 判断是否需要拦截
     */
    public Map<String, List<String>> run() {
//        if (notExcludedTableCondition(invocation, excludedMapperPath) &&
//                (monitoredTableCondition(monitoredTableList))) {
//            return exec();
//        }
        return new HashMap<>();
    }

    protected abstract Map<String, List<String>> exec();


}
