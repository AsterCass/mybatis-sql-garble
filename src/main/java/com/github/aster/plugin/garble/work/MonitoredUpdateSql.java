package com.github.aster.plugin.garble.work;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonitoredUpdateSql extends MonitoredWork {

    private static final String TARGET_REGEX = " set ";


    public MonitoredUpdateSql(Invocation invocation, String updateFlagVolName,
                              List<String> monitoredTableList, String excludedMapperPath) {
        super(invocation, updateFlagVolName, monitoredTableList, excludedMapperPath);
    }

    @Override
    public String exec() {
        Pattern setPat = Pattern.compile(TARGET_REGEX);
        Matcher sqlMatcher = setPat.matcher(sql);
        String newSql = sqlMatcher.replaceFirst(TARGET_REGEX + table + "."+updateFlagVolName+"=1, ");
        final Object[] args2 = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args2[0];
        Object parameterObject2 = args2[1];
        BoundSql newBoundSql = statement.getBoundSql(parameterObject2);
        MappedStatement newStatement = newMappedStatement(statement, new BoundSqlSqlSource(newBoundSql));
        MetaObject msObject = MetaObject.forObject(
                newStatement,
                new DefaultObjectFactory(),
                new DefaultObjectWrapperFactory(),
                new DefaultReflectorFactory());
        msObject.setValue("sqlSource.boundSql.sql", newSql);
        args2[0] = newStatement;
        return "";
    }

    /**
     * 构建新map
     */
    private static MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder =
                new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    /**
     * 定义一个内部辅助类，作用是包装sq
     */
    static class BoundSqlSqlSource implements SqlSource {

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
