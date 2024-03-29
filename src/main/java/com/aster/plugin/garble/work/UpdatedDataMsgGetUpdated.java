package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.UpdatedDataMsgProperty;
import com.aster.plugin.garble.sql.SelectSqlCube;
import com.aster.plugin.garble.sql.UpdateSqlCube;
import com.aster.plugin.garble.util.ExecutorUtil;
import com.aster.plugin.garble.util.MappedStatementUtil;
import com.aster.plugin.garble.util.SqlUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public class UpdatedDataMsgGetUpdated extends UpdatedDataMsgAbstract {


    public UpdatedDataMsgGetUpdated(Invocation invocation, UpdatedDataMsgProperty property) {
        super(invocation, property);
    }


    @Override
    public Map<String, List<String>> exec() {
        try {

            //获取查询更新行语句
            Map<String, String> sqlMap = SelectSqlCube.getUpdatedRowListSql(crossGarbleTableSet, monitoredTableMap,
                    monitoredTableUpdateFlagColMap);
            //获取更新行
            Map<String, List<String>> updatedColMap = new HashMap<>();
            Map<String, List<String>> outputUpdatedColMap = new HashMap<>();
            for (String table : sqlMap.keySet()) {
                BoundSql newBoundSql = new BoundSql(
                        mappedStatement.getConfiguration(), sqlMap.get(table), new ArrayList<>(), new Object());
                ResultMap newResultMap = new ResultMap.Builder(mappedStatement.getConfiguration(),
                        mappedStatement.getId() + MappedStatementUtil.SELECT,
                        String.class, new ArrayList<>()).build();
                MappedStatement getUpdatedRowsMs = MappedStatementUtil.newMappedStatement(
                        mappedStatement, mappedStatement.getId() + MappedStatementUtil.SELECT,
                        new BoundSqlSqlSource(newBoundSql), Collections.singletonList(newResultMap),
                        SqlCommandType.SELECT);
                List<String> resultList = ExecutorUtil.executeSelectRow(
                        sqlMap.get(table), executor, getUpdatedRowsMs, newBoundSql, null);
                if (null != resultList && 0 != resultList.size()) {
                    outputUpdatedColMap.put(
                            SqlUtil.getGarbleTableFromFullName(schema, table).getSelfAdaptionName(schema),
                            resultList);
                    updatedColMap.put(table, resultList);
                }
            }
            //获取回滚语句
            Map<String, String> rollBackMap = UpdateSqlCube.getFlagRollBackList(updatedColMap,
                    monitoredTableMap, monitoredTableUpdateFlagColMap);
            //数据回滚
            for (String table : rollBackMap.keySet()) {
                BoundSql newBoundSql = new BoundSql(
                        mappedStatement.getConfiguration(), rollBackMap.get(table), new ArrayList<>(), new Object());
                ResultMap newResultMap = new ResultMap.Builder(mappedStatement.getConfiguration(),
                        mappedStatement.getId() + MappedStatementUtil.ROLLBACK,
                        String.class, new ArrayList<>()).build();
                MappedStatement getUpdatedRowsMs = MappedStatementUtil.newMappedStatement(
                        mappedStatement, mappedStatement.getId() + MappedStatementUtil.ROLLBACK,
                        new BoundSqlSqlSource(newBoundSql), Collections.singletonList(newResultMap),
                        SqlCommandType.UPDATE);
                ExecutorUtil.executeUpdatedRow(rollBackMap.get(table), executor, getUpdatedRowsMs);
            }


            return outputUpdatedColMap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new HashMap<>();
        }
    }
}
