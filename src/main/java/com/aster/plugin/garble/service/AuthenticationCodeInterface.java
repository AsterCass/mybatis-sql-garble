package com.aster.plugin.garble.service;

import com.aster.plugin.garble.enums.AuthenticationStrategyEnum;

/**
 * @author astercasc
 */
public interface AuthenticationCodeInterface {

    /**
     * 后去权限code，用于和配置字段相比较
     * {@link AuthenticationStrategyEnum}
     * 如果使用的是AuthenticationStrategyEnum.BOOLEAN_AND需要传入的为纯数字的字符串
     * 方法将会在查询监控表的时候依据指定的及authentication strategy，根据此方法的的返回值进行鉴权
     *
     * @return 鉴权code
     */
    String authenticationCodeBuilder();

}
