package com.aster.plugin.garble.work;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.exception.GarbleRuntimeException;
import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import com.aster.plugin.garble.service.AuthenticationCodeBuilder;
import com.aster.plugin.garble.sql.SelectSqlCube;
import com.aster.plugin.garble.util.SqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.InvocationTargetException;
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
     * 鉴权code 表名对应的鉴权code
     */
    protected Map<String, String> monitoredTableAuthCodeMap;

    /**
     * builder
     */
    public AuthenticationFilterSelectAbstract(
            Invocation invocation, AuthenticationFilterSelectProperty property,
            Map<Method, Object> methodForAuthCodeSelect) {

        //基础数据赋值
        super(invocation);

        //基本验证
        if (null == property.getMonitoredTableList() || 0 == property.getMonitoredTableList().size()) {
            throw new GarbleParamException("添加查询鉴权需求但是未检测到监控表配置【monitoredTableList】");
        }

        //基础表名赋值
        this.monitoredTableList = new ArrayList<>();
        for (String tableName : property.getMonitoredTableList()) {
            GarbleTable garbleTable = SqlUtil.getGarbleTableFromFullName(schema, tableName);
            monitoredTableList.add(garbleTable.getFullName());
            this.monitoredTableSet.add(garbleTable);
        }

        //这里全部转小写，后面各种操作，大小写不太方便
        if (null != property.getDefaultAuthColName()) {
            this.defaultAuthColName = property.getDefaultAuthColName().toLowerCase();
        } else {
            this.defaultAuthColName = null;
        }
        if (null != property.getDefaultAuthStrategy()) {
            this.defaultAuthStrategy = property.getDefaultAuthStrategy();
        } else {
            this.defaultAuthStrategy = null;
        }

        //默认值导入
        this.monitoredTableAuthColMap = new HashMap<>();
        this.monitoredTableAuthStrategyMap = new HashMap<>();
        for (GarbleTable garbleTable : this.monitoredTableSet) {
            //权限标记列
            String colMapContainTable = null;
            if (null != property.getMonitoredTableAuthColMap()) {
                colMapContainTable = SqlUtil.garbleContain(
                        new ArrayList<>(property.getMonitoredTableAuthColMap().keySet()), garbleTable, schema);
            }
            if (null != property.getMonitoredTableAuthColMap() && null != colMapContainTable) {
                monitoredTableAuthColMap.put(garbleTable.getFullName(),
                        property.getMonitoredTableAuthColMap().get(colMapContainTable));
            } else if (null == this.defaultAuthColName) {
                throw new GarbleParamException(
                        String.format("monitoredTableList中的[%s]未在monitoredTableAuthColMap中中查询到相应的key, " +
                                "defaultAuthColName也未标明默认的鉴权列, 无法为该表配置鉴权", garbleTable.getSimpleName()));
            } else {
                monitoredTableAuthColMap.put(garbleTable.getFullName(), this.defaultAuthColName);
            }

            //权限策略
            String strategyMapContainTable = null;
            if (null != property.getMonitoredTableAuthStrategyMap()) {
                strategyMapContainTable = SqlUtil.garbleContain(
                        new ArrayList<>(property.getMonitoredTableAuthStrategyMap().keySet()), garbleTable, schema);
            }
            if (null != property.getMonitoredTableAuthStrategyMap() && null != strategyMapContainTable) {
                monitoredTableAuthStrategyMap.put(garbleTable.getFullName(),
                        property.getMonitoredTableAuthStrategyMap().get(strategyMapContainTable));

            } else if (null == this.defaultAuthStrategy) {
                throw new GarbleParamException(
                        String.format("monitoredTableList中的[%s]未在monitoredTableAuthStrategyMap中中查询到相应的key, " +
                                "defaultAuthStrategy默认值也未标明默认的鉴权策略, 无法为该表配置鉴权",
                                garbleTable.getSimpleName()));
            } else {
                monitoredTableAuthStrategyMap.put(garbleTable.getFullName(), defaultAuthStrategy);
            }
        }

        //回调方法
        this.methodForAuthCodeSelect = methodForAuthCodeSelect;

        //忽视的sql的mapper路径
        this.excludedMapperPath = property.getExcludedMapperPath();

    }


    private void setTableAuthCodeMap() {
        try {
            monitoredTableAuthCodeMap = new HashMap<>();
            //先执行所有实现AuthenticationCodeInterface的方法
            HashMap<String, String> annTableRegAuthCodeMap = new HashMap<>();
            //此methodList至少为1个, 校验在项目初始化时完成 SpecifiedMethodGenerator.loadAuthCodeBySubTypes
            for (Method method : methodForAuthCodeSelect.keySet()) {
                Object code = method.invoke(methodForAuthCodeSelect.get(method));
                String authCode;
                if (code instanceof String) {
                    authCode = (String) code;
                    for (String tableReg : method.getAnnotation(AuthenticationCodeBuilder.class).tables()) {
                        annTableRegAuthCodeMap.put(tableReg, authCode);
                    }
                } else {
                    throw new GarbleParamException("鉴权code获取方法返回值需为String类型");
                }
            }
            for (String table : monitoredTableList) {
                String matchedTableReg = null;
                GarbleTable garbleTable = SqlUtil.getGarbleTableFromFullName(schema, table);
                for (String tableReg : annTableRegAuthCodeMap.keySet()) {
                    if (garbleTable.getTableName().matches(tableReg)) {
                        if (null != matchedTableReg && !matchedTableReg.equals(tableReg)) {
                            throw new GarbleParamException(
                                    String.format("[%s]该正则匹配不具有唯一匹配，匹配项有[%s][%s]...",
                                            SqlUtil.getGarbleTableFromFullName(schema, table).getSimpleName(),
                                            matchedTableReg, tableReg));
                        }
                        matchedTableReg = tableReg;
                    }
                    if (garbleTable.getSimpleName().matches(tableReg)) {
                        if (null != matchedTableReg && !matchedTableReg.equals(tableReg)) {
                            throw new GarbleParamException(
                                    String.format("[%s]该正则匹配不具有唯一匹配，匹配项有[%s][%s]...",
                                            SqlUtil.getGarbleTableFromFullName(schema, table).getSimpleName(),
                                            matchedTableReg, tableReg));
                        }
                        matchedTableReg = tableReg;
                    }
                }
                if (null == matchedTableReg) {
                    throw new GarbleParamException(table +
                            " 该table没有在AuthenticationCodeBuilder注解中被使用, 无法获取鉴权code");
                } else {
                    monitoredTableAuthCodeMap.put(table, annTableRegAuthCodeMap.get(matchedTableReg));
                }
            }

        } catch (InvocationTargetException ex) {
            ex.getTargetException().printStackTrace();
            throw new GarbleRuntimeException(ex.getTargetException().getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new GarbleRuntimeException(ex.getMessage());
        }
    }


    /**
     * 判断是否需要拦截
     */
    public void run() {
        if (notExcludedTableCondition(invocation, excludedMapperPath) &&
                (monitoredTableCondition(monitoredTableSet, new SelectSqlCube()))) {
            setTableAuthCodeMap();
            exec();
        }
    }

    /**
     * execute
     */
    protected abstract void exec();


}
