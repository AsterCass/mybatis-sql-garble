package com.aster.plugin.garble.sql;

import java.util.List;
import java.util.Map;

public class InsertAuthSqlCube extends InsertSqlCube {

    /**
     * 监控表列表
     */
    protected List<String> monitoredTableList;

    /**
     * 监控表和权限标记列
     */
    protected Map<String, String> monitoredTableAuthColMap;

    /**
     * 监控表和授权code
     */
    protected Map<String, String> monitoredTableAuthCodeMap;

    public InsertAuthSqlCube(List<String> monitoredTableList,
                             Map<String, String> monitoredTableAuthColMap,
                             Map<String, String> monitoredTableAuthCodeMap) {
        this.monitoredTableAuthColMap = monitoredTableAuthColMap;
        this.monitoredTableList = monitoredTableList;
        this.monitoredTableAuthCodeMap = monitoredTableAuthCodeMap;
    }


    /**
     * sql添加授权语句
     */
    public String addAuthCode(String sql) {
        //map和list的对应关系已经在 AuthenticationFilterAbstract 的构造函数中验证过了
        return addInsertNumberSet(sql, monitoredTableList, monitoredTableAuthColMap, monitoredTableAuthCodeMap);
    }


}
