package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.enums.AuthenticationStrategyEnum;
import com.aster.plugin.garble.exception.GarbleParamException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author astercasc
 */
public class SelectAuthFilterSqlCube extends SelectSqlCube {

    /**
     * 监控表列表
     */
    protected List<String> monitoredTableList;

    /**
     * 监控表和权限标记列
     */
    protected Map<String, String> monitoredTableAuthColMap;

    /**
     * 监控表和权限策略
     */
    protected Map<String, Integer> monitoredTableAuthStrategyMap;

    /**
     * 监控表和鉴权code
     */
    protected Map<String, String> monitoredTableAuthCodeMap;


    public SelectAuthFilterSqlCube(List<String> monitoredTableList,
                                   Map<String, String> monitoredTableAuthColMap,
                                   Map<String, Integer> monitoredTableAuthStrategyMap,
                                   Map<String, String> monitoredTableAuthCodeMap) {
        this.monitoredTableAuthColMap = monitoredTableAuthColMap;
        this.monitoredTableAuthStrategyMap = monitoredTableAuthStrategyMap;
        this.monitoredTableList = monitoredTableList;
        this.monitoredTableAuthCodeMap = monitoredTableAuthCodeMap;
    }

    /**
     * sql添加鉴权语句
     */
    public String addAuthCode(String sql) {
        //map和list的对应关系已经在 AuthenticationFilterAbstract 的构造函数中验证过了
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            Select selectStatement = (Select) statement;
            if (selectStatement.getSelectBody() instanceof PlainSelect) {
                PlainSelect select = (PlainSelect) selectStatement.getSelectBody();
                crossTableBuilder(select);
            } else {
                throw new GarbleParamException("查询语句Body解析失败: " + sql);
            }
            return selectStatement.toString();
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return sql;
    }

    /**
     * 本层查询解析
     */
    private void crossTableBuilder(PlainSelect select) {
        try {
            //本层的sql中包含的tale, 不包含下层的子查询
            List<GarbleTable> sqlTableList = getTableNameMapInSelectBody(select);
            List<GarbleTable> crossTableList = sqlTableList.stream().filter(table ->
                    monitoredTableList.contains(table.getTableName().replace("`", ""))
            ).collect(Collectors.toList());
            Expression where = select.getWhere();
            if (0 != crossTableList.size()) {
                List<Expression> expressionList = expressionListBuilder(crossTableList);
                if (null != where) {
                    getSubTableInWhere(where);
                    expressionList.add(where);
                }
                Expression expression = andExpressionBuilder(expressionList);
                select.setWhere(expression);
            }
        } catch (GarbleParamException ex) {
            throw new GarbleParamException(ex.getMessage() + " SQL: " + select);
        }
    }

    /**
     * 通过不同的表策略去更新sql
     */
    private List<Expression> expressionListBuilder(List<GarbleTable> crossTableList) {
        List<Expression> expressionList = new ArrayList<>();
        if (null != crossTableList && 0 != crossTableList.size()) {
            for (GarbleTable table : crossTableList) {
                String key = table.getTableName().replace("`", "");
                String col = monitoredTableAuthColMap.get(key);
                String code = monitoredTableAuthCodeMap.get(key);
                Integer strategy = monitoredTableAuthStrategyMap.get(key);
                if (null == col) {
                    throw new GarbleParamException(key + " 该表无法从配置文件中载入数据库鉴权列");
                }
                if (null == code) {
                    throw new GarbleParamException(key + " 该表无法从AuthenticationCodeBuilder获取相应鉴权code");
                }
                if (null == strategy) {
                    throw new GarbleParamException(key + " 该表无法从配置文件中载入鉴权策略");
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
                } else {
                    throw new GarbleParamException(key + " 该表配置文件写入错误，参考AuthenticationStrategyEnum");
                }
            }
        }
        return expressionList;
    }

    /**
     * 通过多个expression构建最终在where内的联合表达式
     */
    private Expression andExpressionBuilder(List<Expression> expressionList) {
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

    /**
     * 获取本层的sql中包含的tale
     */
    private List<GarbleTable> getTableNameMapInSelectBody(PlainSelect select) {
        List<GarbleTable> garbleTableList = new ArrayList<>();
        if (select.getFromItem() instanceof Table) {
            Table priTable = (Table) select.getFromItem();
            if (null == priTable.getAlias()) {
                garbleTableList.add(new GarbleTable(priTable, priTable.getName(),
                        priTable.getSchemaName(), null));
            } else {
                garbleTableList.add(new GarbleTable(priTable, priTable.getName(),
                        priTable.getSchemaName(), priTable.getAlias().getName()));
            }

        } else {
            throw new GarbleParamException("查询语句FormItem解析失败");
        }
        if (null != select.getJoins() && 0 != select.getJoins().size()) {
            for (Join join : select.getJoins()) {
                if (join.getRightItem() instanceof Table) {
                    Table joinTable = (Table) join.getRightItem();
                    if (null == joinTable.getAlias()) {
                        garbleTableList.add(new GarbleTable(joinTable, joinTable.getName(),
                                joinTable.getSchemaName(), null));
                    } else {
                        garbleTableList.add(new GarbleTable(joinTable, joinTable.getName(),
                                joinTable.getSchemaName(), joinTable.getAlias().getName()));
                    }
                } else {
                    throw new GarbleParamException("查询语句JoinFormItem解析失败");
                }
            }
        }
        return garbleTableList;
    }

    /**
     * 查询where中的子查询进行循环
     */
    public void getSubTableInWhere(Expression whereExpression) {
        if (null != whereExpression) {
            Method[] methodList = whereExpression.getClass().getMethods();
            for (Method method : methodList) {
                List<String> rightLeftExpression = Arrays.asList("getRightExpression", "getLeftExpression");
                if (rightLeftExpression.contains(method.getName()) && 0 == method.getParameterTypes().length) {
                    try {
                        Object obj = method.invoke(whereExpression);
                        if (obj instanceof SubSelect) {
                            SubSelect subSelectStat = (SubSelect) obj;
                            if (subSelectStat.getSelectBody() instanceof PlainSelect) {
                                PlainSelect select = (PlainSelect) subSelectStat.getSelectBody();
                                crossTableBuilder(select);
                            } else {
                                throw new GarbleParamException("查询语句Body解析失败");
                            }
                        } else if (obj instanceof Expression) {
                            getSubTableInWhere((Expression) obj);
                        }
                    } catch (InvocationTargetException | IllegalAccessException ex) {
                        throw new GarbleParamException("查询语句InWhere方法调用失败");
                    }
                }
            }
        }
    }


}
