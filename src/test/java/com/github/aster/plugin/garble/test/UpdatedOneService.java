package com.github.aster.plugin.garble.test;

import com.github.aster.plugin.garble.service.DealWithUpdated;
import com.github.aster.plugin.garble.service.DealWithUpdatedInterface;

import java.util.List;
import java.util.Map;

public class UpdatedOneService implements DealWithUpdatedInterface {

    @DealWithUpdated(priority = 1)
    @Override
    public void execute(Map<String, List<String>> updatedTableMap) {
        System.out.println("1111");
    }


}
