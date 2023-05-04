package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleRuntimeException;
import com.aster.plugin.garble.util.SqlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.ValueListExpression;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author astercasc
 */
public class UpdateSqlCube extends BaseSqlCube {

    /**
     * 获取所有表名
     *
     * @param sql sql
     * @return 简单表名，不包含schema
     */
    @Override
    @Deprecated
    public List<String> getTableList(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            //拦截器拦截update也是会拦截insert
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
    @Deprecated
    public static Map<GarbleTable, String> getUpdateTableAliasMap(String sql, String defaultSchema) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                Table mainTable = updateStatement.getTable();
                GarbleTable mainGarbleTable = new GarbleTable(
                        mainTable.getName(), mainTable.getSchemaName(), defaultSchema);
                Map<GarbleTable, String> nameAliasMap = new HashMap<>();
                if (null == mainTable.getAlias()) {
                    nameAliasMap.put(mainGarbleTable, mainTable.getName());
                } else {
                    nameAliasMap.put(mainGarbleTable, mainTable.getAlias().getName());
                }
                if (null != updateStatement.getStartJoins() && 0 != updateStatement.getStartJoins().size()) {
                    for (Join join : updateStatement.getStartJoins()) {
                        Table rightTable = new Table();
                        if (join.getRightItem() instanceof Table) {
                            rightTable = (Table) join.getRightItem();
                        }
                        GarbleTable rightGarbleTable = new GarbleTable(
                                rightTable.getName(), rightTable.getSchemaName(), defaultSchema);
                        if (null == rightTable.getAlias()) {
                            nameAliasMap.put(rightGarbleTable, rightTable.getName());
                        } else {
                            nameAliasMap.put(rightGarbleTable, rightTable.getAlias().getName());
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
     * 添加set
     */
    public static String addUpdateSet(String sql, String defaultSchema, Collection<GarbleTable> tableList,
                                      Map<String, String> monitoredTableUpdateColMap,
                                      Map<String, String> monitoredTableUpdateColValueMap) {
        try {
            //sql解析
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                //本层的sql中包含的tale, 不包含下层的子查询
                List<GarbleTable> sqlTableList = SqlUtil.getTableNameMapInSqlBody(updateStatement, defaultSchema);
                List<GarbleTable> currentCrossTableList = sqlTableList.stream().filter(table ->
                        tableList.stream().map(GarbleTable::getFullName).collect(Collectors.toList())
                                .contains(table.getFullName())).collect(Collectors.toList());
                if (currentCrossTableList.size() > 1) {
                    throw new GarbleRuntimeException("目前暂时不支持同时更新多表的鉴权");
                }
                List<Column> columns = new ArrayList<>();
                //添加col
                if (null != updateStatement.getUpdateSets() && 0 != currentCrossTableList.size()) {
                    List<UpdateSet> updateSets = updateStatement.getUpdateSets();
                    for (UpdateSet updateSet : updateSets) {
                        if (null != updateSet && null != updateSet.getColumns()
                                && 0 != updateSet.getColumns().size()) {
                            columns.addAll(updateSet.getColumns());
                        }
                    }
                    //如果更新语句本身带有更新标志位，那么不对sql进行处理，但是回滚和后置操作不受影响
                    String updateFlagColName =
                            monitoredTableUpdateColMap.get(currentCrossTableList.get(0).getFullName());
                    if (columns.stream().anyMatch(cell -> cell.getColumnName().equals(updateFlagColName))) {
                        return sql;
                    }
                    List<UpdateSet> updateVol = new ArrayList<>();
                    updateVol.add(new UpdateSet(
                            new Column(currentCrossTableList.get(0).getTable(), updateFlagColName),
                            new StringValue(monitoredTableUpdateColValueMap
                                    .get(currentCrossTableList.get(0).getFullName()))));
                    updateStatement.getUpdateSets().addAll(updateVol);
                }
                return updateStatement.toString();
            }
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return sql;
    }

    public static Map<String, String> getFlagRollBackList(Map<String, List<String>> updatedColMap,
                                                          Map<String, String> monitoredTableMap,
                                                          Map<String, String> monitoredTableUpdateFlagColMap) {
        //这里查询暂时没有支持不同schema和数据库的配置
        Map<String, String> sqlMap = new HashMap<>();
        if (null != updatedColMap && 0 != updatedColMap.size()) {
            for (String table : updatedColMap.keySet()) {
                putMap(updatedColMap, monitoredTableMap, monitoredTableUpdateFlagColMap, sqlMap, table);
            }
        }
        return sqlMap;

    }

    private static void putMap(Map<String, List<String>> updatedColMap, Map<String, String> monitoredTableMap,
                               Map<String, String> monitoredTableUpdateFlagColMap,
                               Map<String, String> sqlMap, String table) {
        if (null != updatedColMap.get(table) && 0 != updatedColMap.get(table).size()) {
            String whereColName = monitoredTableMap.get(table);
            String flagColName = monitoredTableUpdateFlagColMap.get(table);
            if (null == flagColName) {
                throw new GarbleRuntimeException("fail to get flag col name");
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
