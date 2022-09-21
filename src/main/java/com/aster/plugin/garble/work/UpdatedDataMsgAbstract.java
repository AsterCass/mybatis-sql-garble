package com.aster.plugin.garble.work;

import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.property.UpdatedDataMsgProperty;
import com.aster.plugin.garble.sql.UpdateSqlCube;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public abstract class UpdatedDataMsgAbstract extends UpdatedDataMsgProperty {


    /**
     * builder
     */
    public UpdatedDataMsgAbstract(Invocation invocation, UpdatedDataMsgProperty property) {
        this.invocation = invocation;
        if (invocation.getTarget() instanceof Executor) {
            this.executor = (Executor) invocation.getTarget();
        }
        if (invocation.getArgs()[0] instanceof MappedStatement) {
            this.mappedStatement = (MappedStatement) invocation.getArgs()[0];
        }


        this.crossTableList = new ArrayList<>();

        if (null != property.getMonitoredTableMap() && 0 != property.getMonitoredTableMap().size()) {
            Map<String, String> lowerMonitoredTableMap = new HashMap<>();
            for (Map.Entry<String, String> entry : property.getMonitoredTableMap().entrySet()) {
                lowerMonitoredTableMap.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
            }
            this.monitoredTableMap = lowerMonitoredTableMap;
            this.monitoredTableList = new ArrayList<>(lowerMonitoredTableMap.keySet());
        } else {
            throw new GarbleParamException("添加更新标记返回需求但是未检测到添加表信息配置");
        }

        //这里全部转小写，后面各种操作，大小写不太方便
        this.monitoredTableUpdateFlagColMap = new HashMap<>();
        this.defaultFlagColName = property.getDefaultFlagColName();
        for (String table : monitoredTableList) {
            if (property.getMonitoredTableUpdateFlagColMap().containsKey(table)) {
                String col = property.getMonitoredTableUpdateFlagColMap().get(table);
                monitoredTableUpdateFlagColMap.put(table.toLowerCase(), col.toLowerCase());
            } else if (null != defaultFlagColName && !"".equals(defaultFlagColName)) {
                monitoredTableUpdateFlagColMap.put(table.toLowerCase(), defaultFlagColName.toLowerCase());
            } else {
                throw new GarbleParamException("【" + table + "】该表没有在monitoredTableUpdateFlagColMap中配置," +
                        "也没有配置默认的更新标记列defaultFlagColName");
            }
        }

        this.excludedMapperPath = property.getExcludedMapperPath();
    }



    /**
     * 判断是否需要拦截
     */
    public Map<String, List<String>> run() {
        if (notExcludedTableCondition(invocation, excludedMapperPath) &&
                (monitoredTableCondition(monitoredTableList, new UpdateSqlCube()))) {
            return exec();
        }
        return new HashMap<>();
    }

    /**
     * execute
     *
     * @return 监控表和变动数据map
     */
    protected abstract Map<String, List<String>> exec();

}
