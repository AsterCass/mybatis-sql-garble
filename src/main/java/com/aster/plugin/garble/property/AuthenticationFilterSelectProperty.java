package com.aster.plugin.garble.property;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
@Data
public class AuthenticationFilterSelectProperty {

    /**
     * 标记实现DealWithUpdatedInterface接口的方法路径，加快加快初始化速度，可以不赋值
     */
    private String dealWithUpdatedPath;

    /**
     * 监控表列表
     */
    protected List<String> monitoredTableList;

    /**
     * 监控表和权限标记列
     */
    protected Map<String, String> monitoredTableAuthColMap;

    /**
     * 监控表的默认权限标记列，当monitoredTableUpdateFlagColMap无法查询到需要监控表的权限标记列的时候，使用默认权限标记列
     */
    protected String defaultAuthColName;

    /**
     * 在此map中的的sql不受到监控，即使包含监控表
     */
    protected List<String> excludedMapperPath;

}
