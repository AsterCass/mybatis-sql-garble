package com.aster.plugin.garble.work;

import com.aster.plugin.garble.sql.UpdateSqlCube;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public abstract class AuthenticationFilterAbstract {

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
     * sql和监控表列表重合的表名
     */
    protected List<String> crossTableList;

    /**
     * 监控表列表
     */
    protected List<String> monitoredTableList;

    /**
     * 监控表和权限标记列
     */
    protected Map<String, String> monitoredTableAuthColMap;

    /**
     * 监控表的默认权限标记列，当monitoredTableUpdateFlagColMap无法查询到需要监控表的权限标记列的时候，使用默认权限标记列
     */
    protected String defaultAuthColName;

    /**
     * 在此map中的的sql不受到监控，即使包含监控表
     */
    protected List<String> excludedMapperPath;

    /**
     * builder
     */
    public AuthenticationFilterAbstract(Invocation invocation, String defaultAuthColName,
                                        List<String> monitoredTableList,
                                        Map<String, String> monitoredTableAuthColMap,
                                        List<String> excludedMapperPath) {

    }

    /**
     * 判断是否在监控表列表中
     * todo 这里可以再抽象一层 把部分参数和方法移动到父类 GarbleWorkAbstract中
     */
    private boolean monitoredTableCondition(List<String> monitoredTableList) {
        boolean inMonitored = false;
        //todo 这里可以把sqlCube抽象，使用getTableList，根据构建类搭配不同实现
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
