package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.UpdatedDataMsgProperty;
import com.aster.plugin.garble.sql.UpdateSqlCube;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public abstract class UpdatedDataMsgAbstract extends UpdatedDataMsgProperty {


    /**
     * builder
     */
    public UpdatedDataMsgAbstract(Invocation invocation, UpdatedDataMsgProperty property) {
        this.invocation = invocation;
        this.crossTableList = new ArrayList<>();
        //这里全部转小写，后面各种操作，大小写不太方便
        if (null != property.getDefaultFlagColName()) {
            this.defaultFlagColName = property.getDefaultFlagColName().toLowerCase();
        } else {
            this.defaultFlagColName = "";
        }
        if (null != monitoredTableMap && 0 != property.getMonitoredTableMap().size()) {
            Map<String, String> lowerMonitoredTableMap = new HashMap<>();
            for (Map.Entry<String, String> entry : property.getMonitoredTableMap().entrySet()) {
                lowerMonitoredTableMap.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
            }
            this.monitoredTableMap = lowerMonitoredTableMap;
            this.monitoredTableList = new ArrayList<>(lowerMonitoredTableMap.keySet());
        } else {
            this.monitoredTableList = new ArrayList<>();
            this.monitoredTableMap = new HashMap<>();
        }
        if (null != property.getMonitoredTableUpdateFlagColMap() &&
                0 != property.getMonitoredTableUpdateFlagColMap().size()) {
            Map<String, String> lowerMonitorTableUpdateFlagColMap = new HashMap<>();
            for (Map.Entry<String, String> entry : property.getMonitoredTableUpdateFlagColMap().entrySet()) {
                lowerMonitorTableUpdateFlagColMap.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
            }
            this.monitoredTableUpdateFlagColMap = lowerMonitorTableUpdateFlagColMap;
        } else {
            this.monitoredTableUpdateFlagColMap = new HashMap<>();
        }
        this.excludedMapperPath = property.getExcludedMapperPath();
        if (invocation.getTarget() instanceof Executor) {
            this.executor = (Executor) invocation.getTarget();
        }
        if (invocation.getArgs()[0] instanceof MappedStatement) {
            this.mappedStatement = (MappedStatement) invocation.getArgs()[0];
        }
    }



    /**
     * 判断是否需要拦截
     */
    public Map<String, List<String>> run() {
        if (notExcludedTableCondition(invocation, excludedMapperPath) &&
                (monitoredTableCondition(monitoredTableList, new UpdateSqlCube()))) {
            return exec();
        }
        return new HashMap<>();
    }

    protected abstract Map<String, List<String>> exec();

}
