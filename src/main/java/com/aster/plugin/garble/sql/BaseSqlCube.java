package com.aster.plugin.garble.sql;


import com.alibaba.fastjson.JSON;
import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.enums.AuthenticationStrategyEnum;
import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.util.SqlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.Model;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author astercasc
 */
public abstract class BaseSqlCube {

    /**
     * 获取所有表名
     *
     * @param sql sql
     * @return 简单表名，不包含schema
     */
    @Deprecated
    public List<String> getTableList(String sql) {
        return new ArrayList<>();
    }

    /**
     * 获取所有表名包装
     *
     * @param sql sql
     * @return 复杂结构表名 包括schema
     */
    public Set<GarbleTable> getGarbleTableList(MappedStatement ms, String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
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
     * 获取本层的sql中包含的tale
     */
    protected List<GarbleTable> getTableNameMapInSqlBody(Model stateModel, String defaultSchema) {

        List<GarbleTable> garbleTableList = new ArrayList<>();
        Table priTable;
        List<Join> joins;
        //table and join's 赋值
        if (stateModel instanceof PlainSelect) {
            FromItem fromItem = ((PlainSelect) stateModel).getFromItem();
            joins = ((PlainSelect) stateModel).getJoins();
            if (fromItem instanceof Table) {
                priTable = (Table) fromItem;
            } else {
                throw new GarbleParamException("查询语句FormItem解析失败");
            }

        } else if (stateModel instanceof Update) {
            Update update = (Update) stateModel;
            priTable = update.getTable();
            joins = ((Update) stateModel).getJoins();
        } else {
            throw new GarbleParamException("解析Sql当前层级类型接鉴别错误");
        }

        //pri table
        garbleTableList.add(getGarbleTableFromTable(priTable, defaultSchema));

        //join tables
        if (null != joins && 0 != joins.size()) {
            for (Join join : joins) {
                if (join.getRightItem() instanceof Table) {
                    Table joinTable = (Table) join.getRightItem();
                    garbleTableList.add(getGarbleTableFromTable(joinTable, defaultSchema));
                } else {
                    throw new GarbleParamException("查询语句JoinFormItem解析失败");
                }
            }
        }
        return garbleTableList;
    }

    /**
     * 通过不同的表策略去更新sql
     */
    protected List<Expression> expressionListBuilder(Map<String, String> monitoredTableAuthColMap,
                                                     Map<String, Integer> monitoredTableAuthStrategyMap,
                                                     Map<String, String> monitoredTableAuthCodeMap,
                                                     List<GarbleTable> crossTableList) {
        List<Expression> expressionList = new ArrayList<>();
        if (null != crossTableList && 0 != crossTableList.size()) {
            for (GarbleTable table : crossTableList) {
                String key = table.getFullName();
                String col = monitoredTableAuthColMap.get(key);
                String code = monitoredTableAuthCodeMap.get(key);
                Integer strategy = monitoredTableAuthStrategyMap.get(key);
                if (null == col) {
                    throw new GarbleParamException(
                            String.format("[%s]该表无法从配置文件中载入数据库鉴权列", key));
                }
                if (null == code) {
                    throw new GarbleParamException(
                            String.format("[%s]该表无法从AuthenticationCodeBuilder获取相应鉴权code", key));
                }
                if (null == strategy) {
                    throw new GarbleParamException(
                            String.format("[%s]该表无法从配置文件中载入鉴权策略", key));
                }
                //这里就不用策略模式了，目前用的话，冗余设计了
                if (AuthenticationStrategyEnum.EQUAL.getCode().equals(strategy)) {
                    EqualsTo equals = new EqualsTo();
                    equals.setLeftExpression(new Column(table.getTable(), col));
                    equals.setRightExpression(new StringValue(code));
                    expressionList.add(equals);
                } else if (AuthenticationStrategyEnum.BOOLEAN_AND.getCode().equals(strategy)) {
                    BitwiseAnd bitwiseAnd = new BitwiseAnd();
                    bitwiseAnd.setLeftExpression(new Column(table.getTable(), col));
                    bitwiseAnd.setRightExpression(new StringValue(code));

                    GreaterThan greaterThan = new GreaterThan();
                    greaterThan.setLeftExpression(bitwiseAnd);
                    greaterThan.setRightExpression(new LongValue(0));
                    expressionList.add(greaterThan);
                } else if (AuthenticationStrategyEnum.INTERSECTION.getCode().equals(strategy)) {
                    InExpression inExpression = new InExpression();
                    inExpression.setLeftExpression(new Column(table.getTable(), col));

                    List<String> codeList = JSON.parseArray(code, String.class);
                    ExpressionList expressions = new ExpressionList();
                    for (String inCode : codeList) {
                        expressions.addExpressions(new StringValue(inCode));
                    }
                    inExpression.setRightItemsList(expressions);
                    expressionList.add(inExpression);
                } else {
                    throw new GarbleParamException(
                            String.format("[%s]该表配置文件写入错误，参考AuthenticationStrategyEnum", key));
                }
            }
        }
        return expressionList;
    }

    /**
     * 通过多个expression构建最终在where内的联合表达式
     */
    protected Expression andExpressionBuilder(List<Expression> expressionList) {
        if (null != expressionList && 0 != expressionList.size()) {
            if (1 == expressionList.size()) {
                return expressionList.get(0);
            }
            AndExpression andExpression = new AndExpression();
            for (int count = 1; count < expressionList.size(); ++count) {
                AndExpression andExp = new AndExpression();
                if (count + 1 == expressionList.size()) {
                    andExpression.setLeftExpression(expressionList.get(count));
                    if (null == andExpression.getRightExpression()) {
                        andExpression.setRightExpression(expressionList.get(count - 1));
                    }
                } else {
                    andExp.setLeftExpression(expressionList.get(count));
                    if (null == andExpression.getRightExpression()) {
                        andExp.setRightExpression(expressionList.get(count - 1));
                    } else {
                        andExp.setRightExpression(andExpression.getRightExpression());
                    }
                    andExpression.setRightExpression(andExp);
                }
            }
            return andExpression;
        }
        return null;
    }

    private GarbleTable getGarbleTableFromTable(Table table, String defaultSchema) {
        String schema = null == table.getSchemaName() ? defaultSchema : table.getSchemaName();
        if (null == table.getAlias()) {
            return new GarbleTable(table, table.getName(), schema, null);
        } else {
            return new GarbleTable(table, table.getName(), schema, table.getAlias().getName());
        }
    }

}
