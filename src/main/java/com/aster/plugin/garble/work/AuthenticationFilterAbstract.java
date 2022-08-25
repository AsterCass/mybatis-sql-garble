package com.aster.plugin.garble.work;

import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.exception.GarbleRuntimeException;
import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import com.aster.plugin.garble.sql.SelectSqlCube;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author astercasc
 */
public abstract class AuthenticationFilterAbstract extends AuthenticationFilterSelectProperty {


    /**
     * 鉴权code
     */
    private final String authCode;

    /**
     * builder
     */
    public AuthenticationFilterAbstract(
            Invocation invocation, AuthenticationFilterSelectProperty property,
            List<Method> methodForAuthCodeSelect) {
        this.invocation = invocation;
        this.crossTableList = new ArrayList<>();
        this.excludedMapperPath = property.getExcludedMapperPath();
        if (invocation.getTarget() instanceof Executor) {
            this.executor = (Executor) invocation.getTarget();
        }
        if (invocation.getArgs()[0] instanceof MappedStatement) {
            this.mappedStatement = (MappedStatement) invocation.getArgs()[0];
        }

        this.monitoredTableList = property.getMonitoredTableList();

        //这里全部转小写，后面各种操作，大小写不太方便
        if (null != property.getDefaultAuthColName()) {
            this.defaultAuthColName = property.getDefaultAuthColName().toLowerCase();
        } else {
            this.defaultAuthColName = "";
        }
        if (null != property.getDefaultAuthStrategy()) {
            this.defaultAuthStrategy = property.getDefaultAuthStrategy();
        } else {
            this.defaultAuthStrategy = 0;
        }

        if (null != monitoredTableList && 0 != monitoredTableList.size()) {
            this.monitoredTableAuthColMap = new HashMap<>();
            this.monitoredTableAuthStrategyMap = new HashMap<>();
            for (String table : monitoredTableList) {
                if (null != property.getMonitoredTableAuthColMap().get(table)) {
                    monitoredTableAuthColMap.put(table, property.getMonitoredTableAuthColMap().get(table));
                } else if ("".equals(defaultAuthColName)) {
                    throw new GarbleParamException(
                            "monitor-table-list监控表中包含monitored-table-auth-col-map未标明的table," +
                                    "或没有给予默认权限标记列default-auth-col-name默认值");
                } else {
                    monitoredTableAuthColMap.put(table, defaultAuthColName);
                }

                if (null != property.getMonitoredTableAuthStrategyMap().get(table)) {
                    monitoredTableAuthStrategyMap.put(table, property.getMonitoredTableAuthStrategyMap().get(table));
                } else if (0 == defaultAuthStrategy) {
                    throw new GarbleParamException(
                            "monitor-table-list监控表中包含monitored-table-auth-strategy-map未标明的table," +
                                    "或没有给予默认权限标记列default-auth-strategy默认值");
                } else {
                    monitoredTableAuthStrategyMap.put(table, defaultAuthStrategy);
                }

            }
        }

        try {
            //此methodList为唯一的一个, 校验在项目初始化时完成 SpecifiedMethodGenerator.loadAuthCodeBySubTypes
            Method method = methodForAuthCodeSelect.get(0);
            Object code = method.invoke(method.getDeclaringClass().getDeclaredConstructor().newInstance());
            if (code instanceof String) {
                this.authCode = (String) code;
            } else {
                throw new GarbleParamException("鉴权code获取方法返回值需为String类型");
            }
        } catch (Exception ex) {
            throw new GarbleRuntimeException(ex);
        }


    }


    /**
     * 判断是否需要拦截
     */
    public void run() {
        if (notExcludedTableCondition(invocation, excludedMapperPath) &&
                (monitoredTableCondition(monitoredTableList, new SelectSqlCube()))) {
            exec(authCode);
        }
    }

    /**
     * execute
     *
     * @param authCode 鉴权code
     */
    protected abstract void exec(String authCode);


}
