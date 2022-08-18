package com.aster.plugin.garble.test;

import com.alibaba.fastjson.JSON;
import com.aster.plugin.garble.service.DealWithUpdated;
import com.aster.plugin.garble.service.DealWithUpdatedInterface;

import java.util.List;
import java.util.Map;

public class UpdatedTwoService implements DealWithUpdatedInterface {

    @DealWithUpdated(priority = 2)
    @Override
    public void execute(Map<String, List<String>> updatedTableMap) {
        System.out.println("2222 " + JSON.toJSONString(updatedTableMap));
    }
}
