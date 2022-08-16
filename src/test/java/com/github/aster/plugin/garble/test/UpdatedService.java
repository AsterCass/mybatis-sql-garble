package com.github.aster.plugin.garble.test;

import com.github.aster.plugin.garble.service.DealWithUpdated;

public class UpdatedService {

    @DealWithUpdated(priority = 1)
    public void test() {
        System.out.println("1111");
    }

    @DealWithUpdated(priority = 2)
    public void test2() {
        System.out.println("2222");
    }

}
