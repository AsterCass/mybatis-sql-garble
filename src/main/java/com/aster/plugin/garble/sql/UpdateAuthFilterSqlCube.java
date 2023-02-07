package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleParamException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.update.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                new BaseSqlWhereCube(defaultSchema, crossGarbleTableSet, monitoredTableAuthColMap,
                        monitoredTableAuthStrategyMap, monitoredTableAuthCodeMap).crossTableBuilder(updateStatement);
                return updateStatement.toString();
            }

        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return sql;
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
