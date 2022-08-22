package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.service.UpdateFlagCol;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author astercasc
 */
public class InsertSqlCube {

    /**
     * 新增默认添加标记位为1，如果单纯使用xml文件更新，则无法检测到
     * 最好的建议还是在数据设计的时候把标记位设置位默认为1
     * todo 优化insert拦截方式，支持拦截xml的insert
     */
    public static void addInsertSet(Invocation invocation) {
        try {
            Object entity = invocation.getArgs()[1];
            if (null == entity) {
                return;
            }
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field property : fields) {
                property.setAccessible(true);
                if (null != property.getAnnotation(UpdateFlagCol.class) &&
                        null == property.get(entity)) {
                    property.set(entity, 1);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 这种方法兼容性更强，兼容使用xml插入的方式，但是会报一个?和集合数量不匹配的异常，暂时没有找到解决方案
     * 奇怪的是update的时候，并不会报这个错误
     */
    @Deprecated
    public static String addInsertSet(String sql, Map<String, String> monitoredTableMap,
                                      Map<String, String> monitoredTableUpdateFlagColMap,
                                      String defaultFlagColName) {
        try {
            //配置数据
            List<String> tableList = new ArrayList<>(monitoredTableMap.keySet());
            //sql解析
            Statement statement = CCJSqlParserUtil.parse(sql);

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


}
