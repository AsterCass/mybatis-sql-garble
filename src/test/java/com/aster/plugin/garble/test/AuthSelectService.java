package com.aster.plugin.garble.test;

import com.alibaba.fastjson.JSON;
import com.aster.plugin.garble.enums.AuthenticationStrategyEnum;
import com.aster.plugin.garble.service.AuthenticationCodeBuilder;
import com.aster.plugin.garble.service.AuthenticationCodeInterface;

import java.util.Arrays;

public class AuthSelectService implements AuthenticationCodeInterface {

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
    // 错误示例 这样会匹配成功多个正则 为了防止用户产生难以定位的bug 多个匹配的情况会报错
//     @AuthenticationCodeBuilder(type = 2, tables = {"user", "^garble_.*$", "^garble\\..*$"})
    @AuthenticationCodeBuilder(type = 2, tables = {"user", "^garble.*$"})
    public String authenticationCodeBuilder() {
        return JSON.toJSONString(Arrays.asList("12345678", "1"));
    }

}
