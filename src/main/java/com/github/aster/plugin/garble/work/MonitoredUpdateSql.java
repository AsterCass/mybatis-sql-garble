package com.github.aster.plugin.garble.work;

import com.github.aster.plugin.garble.util.MappedStatementUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

import java.util.ArrayList;
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
    public List<String> exec() {

        //todo use JSqlP
        Pattern setPat = Pattern.compile(TARGET_REGEX);
        Matcher sqlMatcher = setPat.matcher(sql);
        String newSql = sqlMatcher.replaceFirst(TARGET_REGEX + table + "."+updateFlagVolName+"=1, ");


        final Object[] args2 = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args2[0];
        Object parameterObject2 = args2[1];
        BoundSql newBoundSql = statement.getBoundSql(parameterObject2);
        MappedStatement newStatement = MappedStatementUtil.newMappedStatement(
                statement, statement.getId(), new BoundSqlSqlSource(newBoundSql),
                statement.getResultMaps(), mappedStatement.getSqlCommandType());
        MetaObject msObject = MetaObject.forObject(
                newStatement,
                new DefaultObjectFactory(),
                new DefaultObjectWrapperFactory(),
                new DefaultReflectorFactory());
        msObject.setValue("sqlSource.boundSql.sql", newSql);
        args2[0] = newStatement;
        return new ArrayList<>();
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
