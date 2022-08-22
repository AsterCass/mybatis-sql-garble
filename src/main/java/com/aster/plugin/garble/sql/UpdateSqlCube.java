package com.aster.plugin.garble.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.*;

/**
 * @author astercasc
 */
public class UpdateSqlCube {

    /**
     * 获取所有更新表
     */
    public static List<String> getUpdateTableList(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            //拦截器拦截update也是会拦击insert，目前不拦截insert
            if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                //TablesNamesFinder getTableList 会获取 database schema 等信息，不好匹配
                List<String> nameList = new ArrayList<>();
                nameList.add(updateStatement.getTable().getName());
                if (null != updateStatement.getStartJoins() && 0 != updateStatement.getStartJoins().size()) {
                    for (Join join : updateStatement.getStartJoins()) {
                        if (join.getRightItem() instanceof Table) {
                            nameList.add(((Table) join.getRightItem()).getName());
                        }
                    }
                }
                return nameList;
            }
            if (statement instanceof Insert) {
                Insert insertStatement = (Insert) statement;
                return Collections.singletonList(insertStatement.getTable().getName());
            }
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取更新表的全名和别名的Map
     */
    public static Map<String, String> getUpdateTableAliasMap(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                Map<String, String> nameAliasMap = new HashMap<>();
                if (null == updateStatement.getTable().getAlias()) {
                    nameAliasMap.put(updateStatement.getTable().getName(),
                            updateStatement.getTable().getName());
                } else {
                    nameAliasMap.put(updateStatement.getTable().getAlias().getName(),
                            updateStatement.getTable().getName());
                }
                if (null != updateStatement.getStartJoins() && 0 != updateStatement.getStartJoins().size()) {
                    for (Join join : updateStatement.getStartJoins()) {
                        Table rightTable = new Table();
                        if (join.getRightItem() instanceof Table) {
                            rightTable = (Table) join.getRightItem();
                        }
                        if (null == rightTable.getAlias()) {
                            nameAliasMap.put(rightTable.getName(), rightTable.getName());
                        } else {
                            nameAliasMap.put(rightTable.getAlias().getName(), rightTable.getName());
                        }

                    }
                }
                return nameAliasMap;
            }
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * 将被监视的table的更新标志位写入到sql中
     */
    public static String addUpdateSet(String sql, Map<String, String> monitoredTableMap,
                                      Map<String, String> monitoredTableUpdateFlagColMap,
                                      String defaultFlagColName) {
        try {
            //配置数据
            List<String> tableList = new ArrayList<>(monitoredTableMap.keySet());
            Map<String, String> nameAliasMap = UpdateSqlCube.getUpdateTableAliasMap(sql);
            //sql解析
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                //添加set
                if (null != updateStatement.getUpdateSets() && 0 != updateStatement.getUpdateSets().size()) {
                    List<UpdateSet> updateVol = new ArrayList<>();
                    for (UpdateSet updateSet : updateStatement.getUpdateSets()) {
                        for (int count = 0; count < updateSet.getColumns().size(); ++count) {
                            //包含默认
                            Table table = updateSet.getColumns().get(count).getTable();
                            //getName方法只取表名不取schema名
                            String name = null == table ? updateStatement.getTable().getName() : table.getName();
                            String fullTableName = nameAliasMap.get(name);
                            if (tableList.contains(fullTableName)) {
                                String updateFlagColName = monitoredTableUpdateFlagColMap.get(fullTableName);
                                updateFlagColName = null == updateFlagColName ? defaultFlagColName : updateFlagColName;
                                //如果更新语句本身带有更新标志位，那么不对sql进行处理，但是回滚和后置操作不受影响
                                if(updateSet.getColumns().get(count).getColumnName().equals(updateFlagColName)) {
                                    return sql;
                                }
                                updateVol.add(new UpdateSet(
                                        new Column(new Table(name), updateFlagColName),
                                        new LongValue(1)));
                                //防止由于多字段更新，导致的多次更新标志位
                                tableList.remove(fullTableName);
                            }
                        }
                    }
                    updateStatement.getUpdateSets().addAll(updateVol);
                }
                return updateStatement.toString();
            }

            if (statement instanceof Insert) {
                Insert insertStatement = (Insert) statement;
                boolean containFlag = false;
                String flagColName = defaultFlagColName;
                if (tableList.contains(insertStatement.getTable().getName())) {
                    String mapFlagColName =
                            monitoredTableUpdateFlagColMap.get(insertStatement.getTable().getName());
                    if (null != mapFlagColName) {
                        flagColName = mapFlagColName;
                    }
                } else {
                    return sql;
                }
                for (int count = 0; count < insertStatement.getColumns().size(); ++count) {
                    if (flagColName.equals(insertStatement.getColumns().get(count).getColumnName())) {
                        containFlag = true;
                        ExpressionList exp = insertStatement.getItemsList(ExpressionList.class);
                        List<Expression> expressionList = exp.getExpressions();
                        //兼容普通插入和List插入
                        if (expressionList.get(0) instanceof RowConstructor) {
                            for (Expression expression : expressionList) {
                                RowConstructor rowCon = (RowConstructor) expression;
                                rowCon.getExprList().getExpressions().set(count, new LongValue(1));
                            }
                        } else {
                            expressionList.set(count, new LongValue(1));
                        }
                    }
                }
                if (!containFlag) {
                    insertStatement.addColumns(new Column(flagColName));
                    ExpressionList exp = insertStatement.getItemsList(ExpressionList.class);
                    List<Expression> expressionList = exp.getExpressions();
                    //兼容普通插入和List插入
                    if (expressionList.get(0) instanceof RowConstructor) {
                        for (Expression expression : expressionList) {
                            RowConstructor rowCon = (RowConstructor) expression;
                            rowCon.getExprList().addExpressions(new LongValue(1));
                        }
                    } else {
                        expressionList.add(new LongValue(1));
                    }
                }
                return insertStatement.toString();
            }

        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return sql;
    }

    public static Map<String, String> getFlagRollBackList(Map<String, List<String>> updatedColMap,
                                                          Map<String, String> monitoredTableMap,
                                                          Map<String, String> monitoredTableUpdateFlagColMap,
                                                          String defaultFlagColName) {
        //这里查询暂时没有支持不同schema和数据库的配置
        Map<String, String> sqlMap = new HashMap<>();
        if (null != updatedColMap && 0 != updatedColMap.size()) {
            for (String table : updatedColMap.keySet()) {
                putMap(updatedColMap, monitoredTableMap, monitoredTableUpdateFlagColMap,
                        defaultFlagColName, sqlMap, table);
            }
        }
        return sqlMap;

    }

    private static void putMap(Map<String, List<String>> updatedColMap, Map<String, String> monitoredTableMap,
                               Map<String, String> monitoredTableUpdateFlagColMap, String defaultFlagColName,
                               Map<String, String> sqlMap, String table) {
        if (null != updatedColMap.get(table) && 0 != updatedColMap.get(table).size()) {
            String whereColName = monitoredTableMap.get(table);
            String flagColName = monitoredTableUpdateFlagColMap.get(table);
            if (null == flagColName) {
                flagColName = defaultFlagColName;
            }
            Update update = new Update();
            update.setTable(new Table(table));
            update.addUpdateSet(new Column(flagColName), new LongValue(0));

            Column column = new Column().withColumnName(whereColName);
            InExpression in = new InExpression().withLeftExpression(column);
            ExpressionList list = new ExpressionList();
            for (String whereValue : updatedColMap.get(table)) {
                list.addExpressions(new StringValue(whereValue));
            }
            in.withRightExpression(new ValueListExpression().withExpressionList(list));
            update.setWhere(in);


            sqlMap.put(table, update.toString());

        }
    }


}
