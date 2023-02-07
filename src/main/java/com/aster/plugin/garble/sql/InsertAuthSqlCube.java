package com.aster.plugin.garble.sql;

import com.aster.plugin.garble.bean.GarbleTable;

import java.util.Map;
import java.util.Set;

/**
 * @author astercasc
 */
public class InsertAuthSqlCube extends InsertSqlCube {

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
     * 监控表和授权code
     */
    protected Map<String, String> monitoredTableAuthCodeMap;

    public InsertAuthSqlCube(String defaultSchema,
                             Set<GarbleTable> crossGarbleTableSet,
                             Map<String, String> monitoredTableAuthColMap,
                             Map<String, String> monitoredTableAuthCodeMap) {
        this.defaultSchema = defaultSchema;
        this.monitoredTableAuthColMap = monitoredTableAuthColMap;
        this.crossGarbleTableSet = crossGarbleTableSet;
        this.monitoredTableAuthCodeMap = monitoredTableAuthCodeMap;
    }


    /**
     * sql添加授权语句
     */
    public String addAuthCode(String sql) {
        //map和list的对应关系已经在 AuthenticationFilterAbstract 的构造函数中验证过了
        return addInsertNumberSet(sql, crossGarbleTableSet, defaultSchema,
                monitoredTableAuthColMap, monitoredTableAuthCodeMap);
    }


}
