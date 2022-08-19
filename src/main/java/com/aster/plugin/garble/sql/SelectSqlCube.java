package com.aster.plugin.garble.sql;

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public class SelectSqlCube {


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
                equalsTo.withRightExpression(new StringValue("1"));

                plainSelect.setWhere(equalsTo);

                sqlMap.put(table, plainSelect.toString());
            }
        }
        return sqlMap;

    }
}
