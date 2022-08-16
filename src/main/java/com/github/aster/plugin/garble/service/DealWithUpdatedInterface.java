package com.github.aster.plugin.garble.service;

import java.util.List;
import java.util.Map;

public interface DealWithUpdatedInterface {

    /**
     * 更新的表和更新行的数据Map返回
     */
    void execute(Map<String, List<String>> updatedTableMap);

}
