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
                new BaseSqlWhereCube(defaultSchema, crossGarbleTableSet, monitoredTableAuthColMap,
                        monitoredTableAuthStrategyMap, monitoredTableAuthCodeMap).crossTableBuilder(select);
            } else {
                throw new GarbleParamException("查询语句Body解析失败: " + sql);
            }
            return selectStatement.toString();
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return sql;
    }


}
