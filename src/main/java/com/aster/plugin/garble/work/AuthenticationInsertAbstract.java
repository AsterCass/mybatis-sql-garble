package com.aster.plugin.garble.work;

import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.exception.GarbleRuntimeException;
import com.aster.plugin.garble.property.AuthenticationInsertProperty;
import com.aster.plugin.garble.service.AuthenticationCodeBuilder;
import com.aster.plugin.garble.sql.SelectSqlCube;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public abstract class AuthenticationInsertAbstract extends AuthenticationInsertProperty {


    /**
     * 鉴权code
     */
    protected Map<String, String> monitoredTableAuthCodeMap;

    /**
     * builder
     */
    public AuthenticationInsertAbstract(
            Invocation invocation, AuthenticationInsertProperty property,
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

        if (null != monitoredTableList && 0 != monitoredTableList.size()) {
            this.monitoredTableAuthColMap = new HashMap<>();
            for (String table : monitoredTableList) {
                if (null != property.getMonitoredTableAuthColMap() &&
                        null != property.getMonitoredTableAuthColMap().get(table)) {
                    monitoredTableAuthColMap.put(table, property.getMonitoredTableAuthColMap().get(table));
                } else if ("".equals(defaultAuthColName)) {
                    throw new GarbleParamException(
                            "monitor-table-list监控表中包含monitored-table-auth-col-map未标明的table," +
                                    "或没有给予默认权限标记列default-auth-col-name默认值");
                } else {
                    monitoredTableAuthColMap.put(table, defaultAuthColName);
                }

            }
        } else {
            throw new GarbleParamException("添加授权需求但是未检测到授权监控表配置");
        }

        try {
            monitoredTableAuthCodeMap = new HashMap<>();
            //此methodList至少为1个, 校验在项目初始化时完成 SpecifiedMethodGenerator.loadAuthCodeBySubTypes
            HashMap<String, String> annTableAuthCodeMap = new HashMap<>();
            for (Method method : methodForAuthCodeSelect) {
                Object code = method.invoke(method.getDeclaringClass().getDeclaredConstructor().newInstance());
                String authCode;
                if (code instanceof String) {
                    authCode = (String) code;
                    for (String table : method.getAnnotation(AuthenticationCodeBuilder.class).tables()) {
                        annTableAuthCodeMap.put(table, authCode);
                    }
                } else {
                    throw new GarbleParamException("授权code获取方法返回值需为String类型");
                }
            }
            for (String table : monitoredTableList) {
                if (null == annTableAuthCodeMap.get(table)) {
                    throw new GarbleParamException(table +
                            " 该table没有在AuthenticationCodeBuilder注解中被使用, 无法获取授权code");
                } else {
                    monitoredTableAuthCodeMap.put(table, annTableAuthCodeMap.get(table));
                }
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
            exec();
        }
    }

    /**
     * execute
     */
    protected abstract void exec();


}
