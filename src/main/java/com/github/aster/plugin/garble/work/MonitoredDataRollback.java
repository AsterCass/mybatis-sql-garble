package com.github.aster.plugin.garble.work;

import com.github.aster.plugin.garble.util.ExecutorUtil;
import com.github.aster.plugin.garble.util.MappedStatementUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonitoredDataRollback extends MonitoredWork {


    private MonitoredDataRollback() {
        super();
    }

    public MonitoredDataRollback(Invocation invocation, String updateFlagVolName,
                                 List<String> monitoredTableList, String excludedMapperPath) {
        super(invocation, updateFlagVolName, monitoredTableList, excludedMapperPath);
    }


    @Override
    public List<String> exec() {
        try {

            String newSql = "select id from " + table + "  where " + updateFlagVolName + " = 1";

            BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), newSql, new ArrayList<>(), new Object());

            ResultMap newResultMap = new ResultMap.Builder(mappedStatement.getConfiguration(),
                    mappedStatement.getId() + MappedStatementUtil.ROLLBACK,
                    String.class, new ArrayList<>()).build();


            MappedStatement getUpdatedRowsMs = MappedStatementUtil.newMappedStatement(
                    mappedStatement, mappedStatement.getId() + MappedStatementUtil.ROLLBACK,
                    new MonitoredUpdateSql.BoundSqlSqlSource(newBoundSql), Collections.singletonList(newResultMap),
                    SqlCommandType.UPDATE);

            return ExecutorUtil.executeAutoCount(newSql, executor, getUpdatedRowsMs, newBoundSql, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
}
