package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.service.UpdateFlagCol;
import com.aster.plugin.garble.util.SqlUtil;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author astercasc
 */
public class InsertSqlCube extends BaseSqlCube {

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
     * 新增默认添加标记位为1，如果单纯使用xml文件更新，则无法检测到
     * 最好的建议还是在数据设计的时候把标记位设置位默认为1
     */
    @Deprecated
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
     * 这种方法兼容性更强，兼容使用xml插入的方式
     * 目前不会修改已有的列，如果监控列在已有sql中已经处理了，那么不会覆盖
     * 如果之后有逻辑需要覆盖，不能照葫芦画瓢因为直接修改值会造成 - 如果使用的是mybatis参数传入的话则值和对象数量不匹配的问题
     * 如果要修改sql中存在的列的值，参考
     * <a href="https://www.cnblogs.com/flysand/p/9274997.html">https://www.cnblogs.com/flysand/p/9274997.html</a>
     */
    public static String addInsertNumberSet(String sql, Set<GarbleTable> crossGarbleTableSet,
                                            String defaultSchema,
                                            Map<String, String> tableColMap,
                                            Map<String, String> tableValueMap) {
        try {
            //sql解析
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Insert) {
                Insert insertStatement = (Insert) statement;
                GarbleTable thisGrableTable = SqlUtil.getGarbleTableFromFullName(
                        defaultSchema, insertStatement.getTable().getName());
                if (!crossGarbleTableSet.stream().map(GarbleTable::getFullName).collect(Collectors.toList())
                        .contains(thisGrableTable.getFullName())) {
                    return sql;
                }
                String flagColName = tableColMap.get(thisGrableTable.getFullName());
                String value = tableValueMap.get(thisGrableTable.getFullName());
                for (int count = 0; count < insertStatement.getColumns().size(); ++count) {
                    //是否本来的sql中就已经包含了鉴权列
                    if (flagColName.equals(insertStatement.getColumns().get(count).getColumnName())) {
                        //20220920修改：如果本来就包含鉴权列那么直接return 不再修改sql
                        return sql;
                    }
                }
                insertStatement.addColumns(new Column(flagColName));
                ExpressionList exp = insertStatement.getItemsList(ExpressionList.class);
                List<Expression> expressionList = exp.getExpressions();
                //兼容普通插入和List插入
                if (expressionList.get(0) instanceof RowConstructor) {
                    for (Expression expression : expressionList) {
                        RowConstructor rowCon = (RowConstructor) expression;
                        rowCon.getExprList().addExpressions(new LongValue(value));
                    }
                } else {
                    expressionList.add(new LongValue(value));
                }
                return insertStatement.toString();
            }

        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return sql;
    }


}
