package com.aster.plugin.garble.service;

import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public interface DealWithUpdatedInterface {

    /**
     * 更新的表和更新行的数据Map返回
     * @param updatedTableMap 更新数据
     */
    void execute(Map<String, List<String>> updatedTableMap);

}
