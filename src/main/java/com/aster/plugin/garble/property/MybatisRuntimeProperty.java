package com.aster.plugin.garble.property;

import com.aster.plugin.garble.sql.BaseSqlCube;
import lombok.Data;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.util.List;

/**
 * mybatis基本运行时属性
 *
 * @author astercasc
 */
@Data
public class MybatisRuntimeProperty {

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
     * 判断是否需要排除
     */
    protected boolean notExcludedTableCondition(Invocation invocation, List<String> excludedMapperPath) {
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
     * 判断是否在监控表列表中
     */
    protected boolean monitoredTableCondition(List<String> monitoredTableList, BaseSqlCube sqlCube) {
        boolean inMonitored = false;
        List<String> tableList = sqlCube.getTableList(sql);
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


}
