package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleParamException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author astercasc
 */
public class UpdateAuthFilterSqlCube extends UpdateSqlCube {

    /**
     * 默认schema
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

    public UpdateAuthFilterSqlCube(String defaultSchema,
                                   Set<GarbleTable> crossGarbleTableSet,
                                   Map<String, String> monitoredTableAuthColMap,
                                   Map<String, Integer> monitoredTableAuthStrategyMap,
                                   Map<String, String> monitoredTableAuthCodeMap) {
        this.defaultSchema = defaultSchema;
        this.crossGarbleTableSet = crossGarbleTableSet;
        this.monitoredTableAuthColMap = monitoredTableAuthColMap;
        this.monitoredTableAuthStrategyMap = monitoredTableAuthStrategyMap;
        this.monitoredTableAuthCodeMap = monitoredTableAuthCodeMap;
    }

    /**
     * sql添加鉴权语句
     */
    public String addAuthCode(String sql) {
        try {
            //sql解析
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                //本层的sql中包含的tale
                List<GarbleTable> garbleTableList = getTableNameMapInSqlBody(updateStatement, defaultSchema);
                List<GarbleTable> currentCrossTableList = garbleTableList.stream().filter(table ->
                        crossGarbleTableSet.stream().map(GarbleTable::getFullName).collect(Collectors.toList())
                                .contains(table.getFullName())).collect(Collectors.toList());
                //重构where内查询条件, 增加auth过滤
                Expression where = updateStatement.getWhere();
                if (0 != crossGarbleTableSet.size()) {
                    //构建auth过滤的表达式
                    List<Expression> expressionList = expressionListBuilder(monitoredTableAuthColMap,
                            monitoredTableAuthStrategyMap, monitoredTableAuthCodeMap, currentCrossTableList);
                    //如果原表达式式没有where直接插入构建的auth表达式, 否则需要深度优先搜索向下重构之前的where, 再插入构建的auth表达式
                    if (null != where) {
                        getSubTableInWhere(where);
                        //添加括号 防止出现条件中带有or导致的or, and连用导致的逻辑谬误
                        Parenthesis par = new Parenthesis(where);
                        expressionList.add(par);
                    }
                    Expression expression = andExpressionBuilder(expressionList);
                    updateStatement.setWhere(expression);
                }

                return updateStatement.toString();
            }

        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return sql;
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


    /**
     * 获取本层的sql中包含的tale
     */
    private List<GarbleTable> getTableNameMapInSelectBody(Update update) {
        List<GarbleTable> garbleTableList = new ArrayList<>();
        Table priTable = update.getTable();
        //todo select 中 以逗号分割查询表的情况是否考虑到，startjoin 和table的区别, update join 的情况
        if (null == priTable.getAlias()) {
            garbleTableList.add(new GarbleTable(priTable, priTable.getName(),
                    priTable.getSchemaName(), null));
        } else {
            garbleTableList.add(new GarbleTable(priTable, priTable.getName(),
                    priTable.getSchemaName(), priTable.getAlias().getName()));
        }
        if (null != update.getJoins() && 0 != update.getJoins().size()) {
            for (Join join : update.getJoins()) {
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


}
