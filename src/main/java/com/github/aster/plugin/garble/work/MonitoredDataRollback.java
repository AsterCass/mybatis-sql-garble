package com.github.aster.plugin.garble.work;

import com.github.aster.plugin.garble.sql.SelectSqlCube;
import com.github.aster.plugin.garble.util.ExecutorUtil;
import com.github.aster.plugin.garble.util.MappedStatementUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;

import java.util.*;

public class MonitoredDataRollback extends MonitoredWork {


    public MonitoredDataRollback(Invocation invocation, String updateFlagColName,
                                 Map<String, String> monitoredTableMap,
                                 Map<String, String> monitoredTableUpdateFlagColMap,
                                 List<String> excludedMapperPath) {
        super(invocation, updateFlagColName, monitoredTableMap,
                monitoredTableUpdateFlagColMap, excludedMapperPath);
    }


    @Override
    public Map<String, List<String>> exec() {
        try {

            //获取更新行

            Map<String, String> sqlMap = SelectSqlCube.getUpdatedRowListSql(crossTableList, monitoredTableMap,
                    monitoredTableUpdateFlagColMap, defaultFlagColName);
            Map<String, List<String>> updatedColMap = new HashMap<>();
            for (String table : sqlMap.keySet()) {
                BoundSql newBoundSql = new BoundSql(
                        mappedStatement.getConfiguration(), sqlMap.get(table), new ArrayList<>(), new Object());
                ResultMap newResultMap = new ResultMap.Builder(mappedStatement.getConfiguration(),
                        mappedStatement.getId() + MappedStatementUtil.ROLLBACK,
                        String.class, new ArrayList<>()).build();
                MappedStatement getUpdatedRowsMs = MappedStatementUtil.newMappedStatement(
                        mappedStatement, mappedStatement.getId() + MappedStatementUtil.ROLLBACK,
                        new MonitoredUpdateSql.BoundSqlSqlSource(newBoundSql), Collections.singletonList(newResultMap),
                        SqlCommandType.UPDATE);
                List<String> resultList = ExecutorUtil.executeUpdatedRow(
                        sqlMap.get(table), executor, getUpdatedRowsMs, newBoundSql, null);
                updatedColMap.put(table, resultList);
            }
            //标记回滚
//            if(null != resultList && 0 != resultList.size()) {
//                //todo roll back
//            }
            return updatedColMap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new HashMap<>();
        }
    }
}
