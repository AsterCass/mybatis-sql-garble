package com.github.aster.plugin.garble.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.util.Properties;

@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class GarbleUpdateInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (invocation.getArgs()[0] instanceof MappedStatement) {
            final Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameterObject = args[1];
            BoundSql boundSql = ms.getBoundSql(parameterObject);
            String sql = boundSql.getSql();
            String lowerLetterSql = sql.toLowerCase()
                    .replace("\n", " ")
                    .replace("\t", " ");
            log.info(lowerLetterSql);
            return invocation.proceed();
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return (target instanceof Executor || target instanceof StatementHandler)
                ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties prop) {
    }


}
