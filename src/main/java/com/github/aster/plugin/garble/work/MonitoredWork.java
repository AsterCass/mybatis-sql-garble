package com.github.aster.plugin.garble.work;

import com.github.aster.plugin.garble.parser.UpdateSqlParser;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.util.ArrayList;
import java.util.List;

public abstract class MonitoredWork {

    protected MappedStatement mappedStatement;

    protected String table;

    protected String sql;

    protected Invocation invocation;

    protected String updateFlagVolName;

    protected List<String> monitoredTableList;

    protected String excludedMapperPath;

    protected Executor executor;

    public MonitoredWork() {

    }

    public MonitoredWork(Invocation invocation, String updateFlagVolName,
                         List<String> monitoredTableList, String excludedMapperPath) {
        this.invocation = invocation;
        this.updateFlagVolName = updateFlagVolName;
        this.monitoredTableList = monitoredTableList;
        this.excludedMapperPath = excludedMapperPath;
        if (invocation.getTarget() instanceof Executor) {
            this.executor = (Executor) invocation.getTarget();
        }
        if (invocation.getArgs()[0] instanceof MappedStatement) {
            this.mappedStatement = (MappedStatement) invocation.getArgs()[0];
        }
    }

    private boolean monitoredTableCondition(List<String> monitoredTableList) {
        List<String> tableList = UpdateSqlParser.getUpdateTable(sql);
        for (String monitoredTable : monitoredTableList) {
            for (String table : tableList) {
                if (table.toLowerCase().equals(monitoredTable.toLowerCase())) {
                    return true;
                }
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
        this.sql = sql.replace("\n", " ").replace("\t", " ");
        return !ms.getId().contains(excludedMapperPath + ".");
    }

    public List<String> run() {
        if (notExcludedTableCondition(invocation, excludedMapperPath) &&
                (monitoredTableCondition(monitoredTableList))) {
            return exec();
        }
        return new ArrayList<>();
    }

    protected abstract List<String> exec();

}
