package com.aster.plugin.garble.service;

import com.aster.plugin.garble.enums.AuthenticationBehaviorEnum;

/**
 * @author astercasc
 */
public interface AuthenticationCodeInterface {

    /**
     * 获取鉴权code，用于和配置字段相比较
     * {@link AuthenticationBehaviorEnum}
     * 如果使用的是AuthenticationBehaviorEnum.BOOLEAN_AND需要传入的为纯数字的字符串
     * 方法将会在查询监控表的时候依据指定的及authentication behavior，根据此方法的的返回值进行鉴权
     *
     * @return 鉴权code
     */
    String authenticationCodeBuilder();

}
