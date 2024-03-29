package com.aster.plugin.garble.test;

import com.alibaba.fastjson.JSON;
import com.aster.plugin.garble.enums.AuthenticationStrategyEnum;
import com.aster.plugin.garble.service.AuthenticationCodeBuilder;
import com.aster.plugin.garble.service.AuthenticationCodeInterface;

import java.util.Arrays;

public class AuthSelectOtherService implements AuthenticationCodeInterface {

    /**
     * 获取鉴权code，用于和配置字段相比较
     * {@link AuthenticationStrategyEnum}
     * <p>
     * 如果使用的是AuthenticationStrategyEnum.BOOLEAN_AND需要传入的为纯数字的字符串
     * 方法将会在查询监控表的时候依据指定的及authentication strategy，根据此方法的的返回值进行鉴权
     *
     * @return 鉴权code
     */
    @Override
    @AuthenticationCodeBuilder(type = 2, tables = {"^garble.garble_task$", "^garble_else.garble_task$"})
    public String authenticationCodeBuilder() {
        return JSON.toJSONString(Arrays.asList("1234567800", "11", "220", "123456"));
    }

}
