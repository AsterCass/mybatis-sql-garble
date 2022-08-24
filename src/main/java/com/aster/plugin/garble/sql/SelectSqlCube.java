package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.exception.GarbleRuntimeException;
import com.aster.plugin.garble.util.SqlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author astercasc
 */
public class SelectSqlCube extends BaseSqlCube {

    /**
     * 获取所有表名
     *
     * @param sql sql
     * @return 简单表名，不包含schema
     */
    @Override
    public List<String> getTableList(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            //这里和update不同,使用TablesNamesFinder查询sql所含的table，因为手动取select类sql所含table确实太麻烦了
            List<String> fullTableList = new TablesNamesFinder().getTableList(statement);
            List<String> tableList = SqlUtil.getTableNameFromFullName(fullTableList);
            if (0 == tableList.size()) {
                throw new GarbleParamException("查询语句Table解析失败" + sql);
            }
            return tableList;
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return new ArrayList<>();
    }


    /**
     * 获取更新表的全名和别名的Map
     * 目前只支持join和where内子查询，其他复杂的查询sql暂时不支持
     */
    public static Map<String, String> getSelectTableAliasMap(String sql) {

        Map<String, String> nameAliasMap = new HashMap<>();

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            Select selectStatement = (Select) statement;
            if (selectStatement.getSelectBody() instanceof PlainSelect) {
                PlainSelect select = (PlainSelect) selectStatement.getSelectBody();
                getSelectTableAliasMapInSelectBody(nameAliasMap, select);
                getSelectTableAliasMapInWhere(sql, nameAliasMap, select.getWhere());
            } else {
                throw new GarbleRuntimeException("查询语句Body解析失败: " + sql);
            }
            return nameAliasMap;
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return new HashMap<>();
    }

    private static void getSelectTableAliasMapInSelectBody(Map<String, String> nameAliasMap, PlainSelect select) {
        if (select.getFromItem() instanceof Table) {
            Table priTable = (Table) select.getFromItem();
            if (null == priTable.getAlias()) {
                nameAliasMap.put(priTable.getName(), priTable.getName());
            } else {
                nameAliasMap.put(priTable.getAlias().getName(), priTable.getName());
            }
        } else {
            throw new GarbleRuntimeException("查询语句FormItem解析失败: " + select);
        }
        if (null != select.getJoins() && 0 != select.getJoins().size()) {
            for (Join join : select.getJoins()) {
                if (join.getRightItem() instanceof Table) {
                    Table joinTable = (Table) join.getRightItem();
                    if (null == joinTable.getAlias()) {
                        nameAliasMap.put(joinTable.getName(), joinTable.getName());
                    } else {
                        nameAliasMap.put(joinTable.getAlias().getName(), joinTable.getName());
                    }
                } else {
                    throw new GarbleRuntimeException("查询语句JoinFormItem解析失败: " + select);
                }
            }
        }
    }

    public static void getSelectTableAliasMapInWhere(
            String sql, Map<String, String> nameAliasMap, Expression whereExpression) {
        if (null != whereExpression) {
            Method[] methodList = whereExpression.getClass().getDeclaredMethods();
            for (Method method : methodList) {
                if ("getRightExpression".equals(method.getName()) && 0 == method.getParameterTypes().length) {
                    try {
                        Object obj = method.invoke(whereExpression);
                        if (obj instanceof SubSelect) {
                            SubSelect subSelectStat = (SubSelect) obj;
                            if (subSelectStat.getSelectBody() instanceof PlainSelect) {
                                PlainSelect select = (PlainSelect) subSelectStat.getSelectBody();
                                getSelectTableAliasMapInSelectBody(nameAliasMap, select);
                                getSelectTableAliasMapInWhere(select.toString(), nameAliasMap, select.getWhere());
                            } else {
                                throw new GarbleRuntimeException("查询语句Body解析失败: " + subSelectStat);
                            }
                        }
                    } catch (InvocationTargetException | IllegalAccessException ex) {
                        throw new GarbleParamException("查询语句InWhere方法调用失败: " + sql);
                    }

                }
            }
        }
    }


    /**
     * 获取已更新数据的sql
     */
    public static Map<String, String> getUpdatedRowListSql(List<String> crossTableList,
                                                           Map<String, String> monitoredTableMap,
                                                           Map<String, String> monitoredTableUpdateFlagColMap,
                                                           String defaultFlagColName) {
        //这里查询暂时没有支持不同schema和数据库的配置
        Map<String, String> sqlMap = new HashMap<>();
        if (null != crossTableList && 0 != crossTableList.size()) {
            for (String table : crossTableList) {
                String returnColName = monitoredTableMap.get(table);
                String flagColName = monitoredTableUpdateFlagColMap.get(table);
                if (null == flagColName) {
                    flagColName = defaultFlagColName;
                }

                PlainSelect plainSelect = new PlainSelect();
                plainSelect.setFromItem(new Table(table));
                plainSelect.addSelectItems(Collections.singletonList(new SelectExpressionItem()
                        .withExpression(new Column(returnColName))));

                Column column = new Column().withColumnName(flagColName);
                EqualsTo equalsTo = new EqualsTo().withLeftExpression(column);
                equalsTo.withRightExpression(new LongValue(1));

                plainSelect.setWhere(equalsTo);

                sqlMap.put(table, plainSelect.toString());
            }
        }
        return sqlMap;

    }
}
