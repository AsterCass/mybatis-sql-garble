package com.aster.plugin.garble.work;

import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.exception.GarbleRuntimeException;
import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import com.aster.plugin.garble.service.AuthenticationCodeBuilder;
import com.aster.plugin.garble.sql.SelectSqlCube;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author astercasc
 */
@Slf4j
public abstract class AuthenticationFilterSelectAbstract extends AuthenticationFilterSelectProperty {


    /**
     * 鉴权code
     */
    protected Map<String, String> monitoredTableAuthCodeMap;

    /**
     * builder
     */
    public AuthenticationFilterSelectAbstract(
            Invocation invocation, AuthenticationFilterSelectProperty property,
            Map<Method, Object> methodForAuthCodeSelect) {

        this.invocation = invocation;
        if (invocation.getTarget() instanceof Executor) {
            this.executor = (Executor) invocation.getTarget();
        }
        if (invocation.getArgs()[0] instanceof MappedStatement) {
            this.mappedStatement = (MappedStatement) invocation.getArgs()[0];
        }


        this.crossTableList = new ArrayList<>();
        this.excludedMapperPath = property.getExcludedMapperPath();
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

                if (null != property.getMonitoredTableAuthStrategyMap() &&
                        null != property.getMonitoredTableAuthStrategyMap().get(table)) {
                    monitoredTableAuthStrategyMap.put(table, property.getMonitoredTableAuthStrategyMap().get(table));
                } else if (0 == defaultAuthStrategy) {
                    throw new GarbleParamException(
                            "monitor-table-list监控表中包含monitored-table-auth-strategy-map未标明的table," +
                                    "或没有给予默认权限标记列default-auth-strategy默认值");
                } else {
                    monitoredTableAuthStrategyMap.put(table, defaultAuthStrategy);
                }

            }
        } else {
            throw new GarbleParamException("添加鉴权需求但是未检测到鉴权监控表配置");
        }
        try {
            monitoredTableAuthCodeMap = new HashMap<>();
            //此methodList至少为1个, 校验在项目初始化时完成 SpecifiedMethodGenerator.loadAuthCodeBySubTypes
            HashMap<String, String> annTableAuthCodeMap = new HashMap<>();
            for (Method method : methodForAuthCodeSelect.keySet()) {
                Object code = method.invoke(methodForAuthCodeSelect.get(method));
                String authCode;
                if (code instanceof String) {
                    authCode = (String) code;
                    for (String table : method.getAnnotation(AuthenticationCodeBuilder.class).tables()) {
                        annTableAuthCodeMap.put(table, authCode);
                    }
                } else {
                    throw new GarbleParamException("鉴权code获取方法返回值需为String类型");
                }
            }
            for (String table : monitoredTableList) {
                if (null == annTableAuthCodeMap.get(table)) {
                    throw new GarbleParamException(table +
                            " 该table没有在AuthenticationCodeBuilder注解中被使用, 无法获取鉴权code");
                } else {
                    monitoredTableAuthCodeMap.put(table, annTableAuthCodeMap.get(table));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
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
