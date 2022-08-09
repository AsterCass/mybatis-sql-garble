package com.github.aster.plugin.garble.work;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.util.List;

public abstract class MonitoredWork {

    protected String table;

    protected String sql;

    protected Invocation invocation;

    protected String updateFlagVolName;

    protected List<String> monitoredTableList;

    protected String excludedMapperPath;

    public MonitoredWork() {

    }

    public MonitoredWork(Invocation invocation, String updateFlagVolName,
                         List<String> monitoredTableList, String excludedMapperPath) {
        this.invocation = invocation;
        this.updateFlagVolName = updateFlagVolName;
        this.monitoredTableList = monitoredTableList;
        this.excludedMapperPath = excludedMapperPath;
    }

    private boolean monitoredTableCondition(List<String> monitoredTableList) {
        for (String table : monitoredTableList) {
            String reg = "^ *update +" + table + " +.*";
            if (sql.matches(reg)) {
                return true;
            }
        }
        return false;
    }

    private boolean notExcludedTableCondition(Invocation invocation, String excludedMapperPath) {
        final Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        String sql = boundSql.getSql();
        this.sql = sql.toLowerCase().replace("\n", " ").replace("\t", " ");
        return !ms.getId().contains(excludedMapperPath + ".");
    }

    public String run() {
        if (monitoredTableCondition(monitoredTableList) &&
                notExcludedTableCondition(invocation, excludedMapperPath)) {
            return exec();
        }
        return "";
    }

    protected abstract String exec();

}
