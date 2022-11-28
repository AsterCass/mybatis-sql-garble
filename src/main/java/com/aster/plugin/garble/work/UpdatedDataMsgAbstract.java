package com.aster.plugin.garble.work;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.property.UpdatedDataMsgProperty;
import com.aster.plugin.garble.sql.UpdateSqlCube;
import com.aster.plugin.garble.util.SqlUtil;
import org.apache.ibatis.plugin.Invocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

        super(invocation);

        if (null != property.getMonitoredTableMap() && 0 != property.getMonitoredTableMap().size()) {
            Map<String, String> lowerMonitoredTableMap = new HashMap<>();
            for (Map.Entry<String, String> entry : property.getMonitoredTableMap().entrySet()) {
                lowerMonitoredTableMap.put(SqlUtil.getGarbleTableFromFullName(schema, entry.getKey()).getFullName(),
                        entry.getValue().toLowerCase());
            }
            this.monitoredTableMap = lowerMonitoredTableMap;
            this.monitoredTableSet = SqlUtil.getGarbleTableFromFullName(
                    schema, new ArrayList<>(lowerMonitoredTableMap.keySet()));
        } else {
            throw new GarbleParamException("添加更新标记返回需求但是未检测到添加表信息配置");
        }


        //这里全部转小写，后面各种操作，大小写不太方便
        this.monitoredTableUpdateFlagColMap = new HashMap<>();

        for (GarbleTable table : monitoredTableSet) {
            //如果property.getMonitoredTableUpdateFlagColMap()没有初始化则使用default值
            if (null == property.getMonitoredTableUpdateFlagColMap()
                    || 0 == property.getMonitoredTableUpdateFlagColMap().size()) {
                if (null != property.getDefaultFlagColName() && !"".equals(property.getDefaultFlagColName())) {
                    monitoredTableUpdateFlagColMap.put(table.getFullName(),
                            property.getDefaultFlagColName().toLowerCase());
                } else {
                    throw new GarbleParamException(String.format("【%s】该表没有在monitoredTableUpdateFlagColMap中配置," +
                            "也没有配置默认的更新标记列defaultFlagColName", table.getFullName()));
                }
                continue;
            }
            //flagColMap不为空则优先使用map中的col
            boolean insertFlag = false;
            for (String flagColTable : property.getMonitoredTableUpdateFlagColMap().keySet()) {
                if (SqlUtil.garbleEqual(flagColTable, table, schema)) {
                    monitoredTableUpdateFlagColMap.put(table.getFullName(),
                            property.getMonitoredTableUpdateFlagColMap().get(flagColTable).toLowerCase());
                    insertFlag = true;
                } else if (null != property.getDefaultFlagColName() && !"".equals(property.getDefaultFlagColName())) {
                    monitoredTableUpdateFlagColMap.put(table.getFullName(),
                            property.getDefaultFlagColName().toLowerCase());
                    insertFlag = true;
                }
            }
            if (!insertFlag) {
                throw new GarbleParamException(String.format("【%s】该表没有在monitoredTableUpdateFlagColMap中配置," +
                        "也没有配置默认的更新标记列defaultFlagColName", table.getSimpleName()));
            }
        }

        //默认更新标记字段 这里只是做一下记录以防万一 上方已经将defaultFlagColName录入到updateFlagColMap当中
        this.defaultFlagColName = property.getDefaultFlagColName();

        //初始化交集
        crossGarbleTableSet = new HashSet<>();

        //忽视的sql的mapper路径
        this.excludedMapperPath = property.getExcludedMapperPath();

    }



    /**
     * 判断是否需要拦截
     */
    public Map<String, List<String>> run() {
        if (notExcludedTableCondition(invocation, excludedMapperPath) &&
                (monitoredTableCondition(monitoredTableSet, new UpdateSqlCube()))) {
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
