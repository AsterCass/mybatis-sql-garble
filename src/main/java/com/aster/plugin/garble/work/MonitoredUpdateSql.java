package com.aster.plugin.garble.work;

import com.aster.plugin.garble.sql.UpdateSqlCube;
import com.aster.plugin.garble.util.MappedStatementUtil;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public class MonitoredUpdateSql extends MonitoredWork {


    public MonitoredUpdateSql(Invocation invocation, String defaultFlagColName,
                              Map<String, String> monitoredTableMap,
                              Map<String, String> monitoredTableUpdateFlagColMap,
                              List<String> excludedMapperPath) {
        super(invocation, defaultFlagColName, monitoredTableMap,
                monitoredTableUpdateFlagColMap, excludedMapperPath);
    }

    @Override
    public Map<String, List<String>> exec() {
        boolean isUpdate = false;
        boolean isInsert = false;
        try {
            Statement stat = CCJSqlParserUtil.parse(sql);
            if (stat instanceof Update) {
                isUpdate = true;
            } else if (stat instanceof Insert) {
                isInsert = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (isUpdate) {
            String newSql = UpdateSqlCube.addUpdateSet(sql, monitoredTableMap,
                    monitoredTableUpdateFlagColMap, defaultFlagColName);
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
        } else if (isInsert) {
            //todo 临时解决
            try {
                Field[] fields = invocation.getArgs()[1].getClass().getDeclaredFields();
                for (Field property : fields) {
                    property.setAccessible(true);
                    if (property.getName().equals("updateRecord")) {
                        property.set(invocation.getArgs()[1], 1);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


//        try {
//            Statement stat = CCJSqlParserUtil.parse(newSql);
//            if (stat instanceof Insert) {
//                Field[] fields = parameterObject2.getClass().getDeclaredFields();
//                for (Field property : fields) {
//                    property.setAccessible(true);
//                    if (property.getName().equals("updateRecord")) {
//                        String name = property.getName();
//                        String type = property.getGenericType().toString();
//                        String firstUpperName = name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
//                        property.setAccessible(true);
//
//
//                        if ("class java.lang.Integer".equals(type)) {
//                            Method method = parameterObject2.getClass()
//                                    .getDeclaredMethod("set" + firstUpperName, Integer.class);
//                            method.invoke(parameterObject2, 1);
//                        }
//
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }


        return new HashMap<>();
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
