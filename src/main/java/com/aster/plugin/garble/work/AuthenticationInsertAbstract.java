package com.aster.plugin.garble.work;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.exception.GarbleRuntimeException;
import com.aster.plugin.garble.property.AuthenticationInsertProperty;
import com.aster.plugin.garble.sql.InsertSqlCube;
import com.aster.plugin.garble.util.SqlUtil;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
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
            Invocation invocation, AuthenticationInsertProperty property) {

        //基础数据赋值
        super(invocation);

        //基本验证
        if (null == property.getMonitoredTableList() || 0 == property.getMonitoredTableList().size()) {
            throw new GarbleParamException("添加插入授权需求但是未检测到监控表配置【monitoredTableList】");
        }

        //基础表名赋值
        this.monitoredTableList = new ArrayList<>();
        for (String tableName : property.getMonitoredTableList()) {
            GarbleTable garbleTable = SqlUtil.getGarbleTableFromFullName(schema, tableName);
            this.monitoredTableList.add(garbleTable.getFullName());
            this.monitoredTableSet.add(garbleTable);
        }

        //这里全部转小写，后面各种操作，大小写不太方便
        if (null != property.getDefaultAuthColName()) {
            this.defaultAuthColName = property.getDefaultAuthColName().toLowerCase();
        } else {
            this.defaultAuthColName = null;
        }

        this.monitoredTableAuthColMap = new HashMap<>();
        for (GarbleTable garbleTable : monitoredTableSet) {
            //权限标记列
            String colMapContainTable = null;
            if (null != property.getMonitoredTableAuthColMap()) {
                colMapContainTable = SqlUtil.garbleContain(
                        new ArrayList<>(property.getMonitoredTableAuthColMap().keySet()), garbleTable, schema);
            }
            if (null != property.getMonitoredTableAuthColMap() && null != colMapContainTable) {
                this.monitoredTableAuthColMap.put(garbleTable.getFullName(),
                        property.getMonitoredTableAuthColMap().get(colMapContainTable));
            } else if (null == defaultAuthColName) {
                throw new GarbleParamException(
                        String.format("monitoredTableList中的[%s]未在monitoredTableAuthColMap中中查询到相应的key, " +
                                "defaultAuthColName也未标明默认的鉴权列, 无法为该表配置鉴权", garbleTable.getSimpleName()));
            } else {
                this.monitoredTableAuthColMap.put(garbleTable.getFullName(), defaultAuthColName);
            }
        }

        //回调方法
        this.methodForAuthCodeInsert = property.getMethodForAuthCodeInsert();

        //忽视的sql的mapper路径
        this.excludedMapperPath = property.getExcludedMapperPath();

    }

    private void setTableAuthCodeMap() {
        try {
            //先执行所有实现AuthenticationCodeInterface的方法
            //此methodList至少为1个, 校验在项目初始化时完成 SpecifiedMethodGenerator.loadAuthCodeBySubTypes
            HashMap<String, String> annTableRegAuthCodeMap = executeMethodForGetAuth(
                    methodForAuthCodeInsert, "授权code获取方法返回值需为String类型");
            this.monitoredTableAuthCodeMap = authRegMatch(annTableRegAuthCodeMap, crossGarbleTableSet,
                    " 该table没有在AuthenticationCodeBuilder注解中被使用, 无法获取授权code");
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
                (monitoredTableCondition(monitoredTableSet, new InsertSqlCube()))) {
            setTableAuthCodeMap();
            exec();
        }
    }

    /**
     * execute
     */
    protected abstract void exec();


}
