package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.util.SqlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * 获取所有garble表名
     *
     * @param sql sql
     * @return 简单表名，不包含schema
     */
    @Override
    public Set<GarbleTable> getGarbleTableList(MappedStatement ms, String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            //这里和update不同,使用TablesNamesFinder查询sql所含的table，因为手动取select类sql所含table确实太麻烦了
            List<String> fullTableList = new TablesNamesFinder().getTableList(statement);
            Set<GarbleTable> tableSet = SqlUtil.getGarbleTableFromFullName(ms, fullTableList);
            if (0 == tableSet.size()) {
                throw new GarbleParamException("查询语句Table解析失败" + sql);
            }
            return tableSet;
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return new HashSet<>();
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
