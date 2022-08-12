package com.github.aster.plugin.garble.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateSqlCube {

    /**
     * 获取所有更新表
     */
    public static List<String> getUpdateTableList(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
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
            Update updateStatement = (Update) statement;
            //添加set
            if (null != updateStatement.getUpdateSets() && 0 != updateStatement.getUpdateSets().size()) {
                List<UpdateSet> updateVol = new ArrayList<>();
                for (UpdateSet updateSet : updateStatement.getUpdateSets()) {
                    for (int count = 0; count < updateSet.getColumns().size(); ++count) {
                        Table table = updateSet.getColumns().get(count).getTable();
                        //getName方法只取表名不取schema名
                        String name = null == table ? updateStatement.getTable().getName() : table.getName();
                        String fullTableName = nameAliasMap.get(name);
                        if (tableList.contains(fullTableName)) {
                            String updateFlagColName = monitoredTableUpdateFlagColMap.get(fullTableName);
                            updateFlagColName = null == updateFlagColName ? defaultFlagColName : updateFlagColName;

                            updateVol.add(new UpdateSet(
                                    new Column(new Table(name), updateFlagColName),
                                    new StringValue("1")));
                            //防止由于多字段更新，导致的多次更新标志位
                            tableList.remove(fullTableName);
                        }
                    }
                }
                updateStatement.getUpdateSets().addAll(updateVol);
            }
            return updateStatement.toString();

        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return sql;
    }


}
