package com.aster.plugin.garble.test;

import com.alibaba.fastjson.JSON;
import com.aster.plugin.garble.service.DealWithUpdated;
import com.aster.plugin.garble.service.DealWithUpdatedInterface;

import java.util.List;
import java.util.Map;

public class UpdatedOneService implements DealWithUpdatedInterface {

    @DealWithUpdated(priority = 1)
    @Override
    public void execute(Map<String, List<String>> updatedTableMap) {
        System.out.println("1:" + JSON.toJSONString(updatedTableMap));
    }

    private void test() {

    }

    public void pubTest() {

    }

    @DealWithUpdated(priority = 3)
    public void pubTestAnn() {

    }

    public void execute() {

    }


}
