package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleParamException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author astercasc
 */
public class SelectAuthFilterSqlCube extends SelectSqlCube {

    /**
     * 当前连接的schema
     */
    protected String defaultSchema;

    /**
     * 监控表列表
     */
    protected Set<GarbleTable> crossGarbleTableSet;

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


    public SelectAuthFilterSqlCube(String defaultSchema,
                                   Set<GarbleTable> crossGarbleTableSet,
                                   Map<String, String> monitoredTableAuthColMap,
                                   Map<String, Integer> monitoredTableAuthStrategyMap,
                                   Map<String, String> monitoredTableAuthCodeMap) {
        this.defaultSchema = defaultSchema;
        this.monitoredTableAuthColMap = monitoredTableAuthColMap;
        this.monitoredTableAuthStrategyMap = monitoredTableAuthStrategyMap;
        this.crossGarbleTableSet = crossGarbleTableSet;
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
            List<GarbleTable> sqlTableList = getTableNameMapInSqlBody(select, defaultSchema);
            List<GarbleTable> currentCrossTableList = sqlTableList.stream().filter(table ->
                    crossGarbleTableSet.stream().map(GarbleTable::getFullName).collect(Collectors.toList())
                            .contains(table.getFullName())).collect(Collectors.toList());
            //重构where内查询条件, 增加auth过滤
            Expression where = select.getWhere();
            if (0 != crossGarbleTableSet.size()) {
                //构建auth过滤的表达式
                List<Expression> expressionList = expressionListBuilder(
                        monitoredTableAuthColMap, monitoredTableAuthStrategyMap,
                        monitoredTableAuthCodeMap, currentCrossTableList);
                //如果原表达式式没有where直接插入构建的auth表达式, 否则需要深度优先搜索向下重构之前的where, 再插入构建的auth表达式
                if (null != where) {
                    getSubTableInWhere(where);
                    //添加括号 防止出现条件中带有or导致的or, and连用导致的逻辑谬误
                    Parenthesis par = new Parenthesis(where);
                    expressionList.add(par);
                }
                Expression expression = andExpressionBuilder(expressionList);
                select.setWhere(expression);
            }
        } catch (GarbleParamException ex) {
            throw new GarbleParamException(ex.getMessage() + " SQL: " + select);
        }
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
