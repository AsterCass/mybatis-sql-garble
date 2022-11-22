package com.aster.plugin.garble.property;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.sql.BaseSqlCube;
import com.aster.plugin.garble.util.MappedStatementUtil;
import com.mysql.cj.jdbc.ConnectionImpl;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * mybatis基本运行时属性
 *
 * @author astercasc
 */
@Data
@NoArgsConstructor
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
     * connect schema
     */
    protected String schema;

    /**
     * sql和监控表列表重合的表名
     */
    @Deprecated
    protected List<String> crossTableList;

    /**
     * sql和监控表列表重合的表包装
     */
    protected Set<GarbleTable> crossGarbleTableSet;

    /**
     * sql表达的所含表set
     */
    protected Set<GarbleTable> sqlGarbleTableSet;

    /**
     * 监控表列表
     */
    protected Set<GarbleTable> monitoredTableSet;


    private static final int NEW_VERSION_INVOCATION_ARG_NUM = 6;

    public MybatisRuntimeProperty(Invocation invocation) {
        this.invocation = invocation;
        if (invocation.getTarget() instanceof Executor) {
            this.executor = (Executor) invocation.getTarget();
        }
        if (invocation.getArgs()[0] instanceof MappedStatement) {
            this.mappedStatement = (MappedStatement) invocation.getArgs()[0];
        }
        try {
            Connection connection = this.mappedStatement.getConfiguration()
                    .getEnvironment().getDataSource().getConnection();
            if (connection instanceof ConnectionImpl) {
                ConnectionImpl con = (ConnectionImpl) connection;
                this.schema = con.getDatabase();
            }
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("connect to sql fail");
        }


        this.crossTableList = new ArrayList<>();
    }

    /**
     * 判断是否需要排除
     */
    protected boolean notExcludedTableCondition(Invocation invocation, List<String> excludedMapperPath) {
        final Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql;
        String sql;
        if (NEW_VERSION_INVOCATION_ARG_NUM == args.length) {
            boundSql = (BoundSql) args[5];
        } else {
            boundSql = ms.getBoundSql(parameterObject);
        }
        sql = boundSql.getSql();
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

    /**
     * 判断是否在监控表列表中
     */
    protected boolean monitoredTableCondition(Set<GarbleTable> monitoredTableList, BaseSqlCube sqlCube) {
        boolean inMonitored = false;
        sqlGarbleTableSet = sqlCube.getGarbleTableList(mappedStatement, sql);
        for (GarbleTable monitoredTable : monitoredTableList) {
            for (GarbleTable table : sqlGarbleTableSet) {
                //当表名和schema都相同的话
                if (table.equal(monitoredTable)) {
                    inMonitored = true;
                    crossGarbleTableSet.add(table);
                }
            }
        }
        return inMonitored;
    }

    /**
     * 新sql导入invocation
     */
    protected void newSqlBuilder(String newSql) {
        final Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameterObject2 = args[1];
        BoundSql newBoundSql;
        if (NEW_VERSION_INVOCATION_ARG_NUM == args.length) {
            newBoundSql = (BoundSql) args[5];
        } else {
            newBoundSql = statement.getBoundSql(parameterObject2);
        }
        MappedStatement newStatement = MappedStatementUtil.newMappedStatement(
                statement, statement.getId(), new BoundSqlSqlSource(newBoundSql),
                statement.getResultMaps(), mappedStatement.getSqlCommandType());
        MetaObject msObject = MetaObject.forObject(
                newStatement,
                new DefaultObjectFactory(),
                new DefaultObjectWrapperFactory(),
                new DefaultReflectorFactory());
        msObject.setValue("sqlSource.boundSql.sql", newSql);
        args[0] = newStatement;
        if (NEW_VERSION_INVOCATION_ARG_NUM == args.length) {
            args[5] = newBoundSql;
        }
    }

    /**
     * 定义一个内部辅助类，作用是包装sq
     */
    protected static class BoundSqlSqlSource implements SqlSource {

        private final BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }

    }


}
