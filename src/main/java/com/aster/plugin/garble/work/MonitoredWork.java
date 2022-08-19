package com.aster.plugin.garble.work;

import com.aster.plugin.garble.sql.UpdateSqlCube;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public abstract class MonitoredWork {

    /**
     * mybatis拦截信息
     */
    protected MappedStatement mappedStatement;

    /**
     * mybatis拦截信息
     */
    protected Invocation invocation;

    /**
     * mybatis拦截信息
     */
    protected Executor executor;

    /**
     * 执行的sql
     */
    protected String sql;

    /**
     * 监控表的默认更新标记位，当monitoredTableUpdateFlagColMap无法查询到需要监控表的更新标记位的时候，使用默认更新标记位
     */
    protected String defaultFlagColName;

    /**
     * 监控表列表
     */
    protected List<String> monitoredTableList;

    /**
     * sql和监控表列表重合的表名
     */
    protected List<String> crossTableList;

    /**
     * 监控表的表名和返回字段名，返回字段一般可以设置为主键
     */
    protected Map<String, String> monitoredTableMap;

    /**
     * 监控表的更新标记
     */
    protected Map<String, String> monitoredTableUpdateFlagColMap;

    /**
     * 在此map中的的sql不受到监控，即使包含监控表
     */
    protected List<String> excludedMapperPath;

    /**
     * builder
     */
    public MonitoredWork(Invocation invocation, String defaultFlagColName,
                         Map<String, String> monitoredTableMap,
                         Map<String, String> monitoredTableUpdateFlagColMap,
                         List<String> excludedMapperPath) {
        this.invocation = invocation;
        this.crossTableList = new ArrayList<>();
        //这里全部转小写，后面各种操作，大小写不太方便
        if (null != defaultFlagColName) {
            this.defaultFlagColName = defaultFlagColName.toLowerCase();
        } else {
            this.defaultFlagColName = "";
        }
        if (null != monitoredTableMap && 0 != monitoredTableMap.size()) {
            Map<String, String> lowerMonitoredTableMap = new HashMap<>();
            for (Map.Entry<String, String> entry : monitoredTableMap.entrySet()) {
                lowerMonitoredTableMap.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
            }
            this.monitoredTableMap = lowerMonitoredTableMap;
            this.monitoredTableList = new ArrayList<>(lowerMonitoredTableMap.keySet());
        } else {
            this.monitoredTableList = new ArrayList<>();
            this.monitoredTableMap = new HashMap<>();
        }
        if (null != monitoredTableUpdateFlagColMap && 0 != monitoredTableUpdateFlagColMap.size()) {
            Map<String, String> lowerMonitorTableUpdateFlagColMap = new HashMap<>();
            for (Map.Entry<String, String> entry : monitoredTableUpdateFlagColMap.entrySet()) {
                lowerMonitorTableUpdateFlagColMap.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
            }
            this.monitoredTableUpdateFlagColMap = lowerMonitorTableUpdateFlagColMap;
        } else {
            this.monitoredTableUpdateFlagColMap = new HashMap<>();
        }
        this.excludedMapperPath = excludedMapperPath;
        if (invocation.getTarget() instanceof Executor) {
            this.executor = (Executor) invocation.getTarget();
        }
        if (invocation.getArgs()[0] instanceof MappedStatement) {
            this.mappedStatement = (MappedStatement) invocation.getArgs()[0];
        }
    }

    /**
     * 判断是否在监控表列表中
     */
    private boolean monitoredTableCondition(List<String> monitoredTableList) {
        boolean inMonitored = false;
        List<String> tableList = UpdateSqlCube.getUpdateTableList(sql);
        for (String monitoredTable : monitoredTableList) {
            for (String table : tableList) {
                if (table.equals(monitoredTable)) {
                    inMonitored = true;
                    crossTableList.add(table);
                }
            }
        }
        return inMonitored;
    }

    /**
     * 判断是否需要排除
     */
    private boolean notExcludedTableCondition(Invocation invocation, List<String> excludedMapperPath) {
        final Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        String sql = boundSql.getSql();
        //这里全部转小写，后面各种操作，大小写不太方便
        this.sql = sql.toLowerCase();
        boolean toGarble = true;
        if (null != excludedMapperPath && 0 != excludedMapperPath.size()) {
            for (String path : excludedMapperPath) {
                if (ms.getId().contains(path + ".")) {
                    toGarble = false;
                    break;
                }
            }
        }
        return toGarble;
    }

    /**
     * 判断是否需要拦截
     */
    public Map<String, List<String>> run() {
        if (notExcludedTableCondition(invocation, excludedMapperPath) &&
                (monitoredTableCondition(monitoredTableList))) {
            return exec();
        }
        return new HashMap<>();
    }

    protected abstract Map<String, List<String>> exec();

}
