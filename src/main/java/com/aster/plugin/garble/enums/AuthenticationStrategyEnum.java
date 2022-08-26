package com.aster.plugin.garble.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author astercasc
 */
@Getter
@AllArgsConstructor
public enum AuthenticationStrategyEnum {

    /**
     * 当传入鉴权code和数据库相应字段相等时
     */
    EQUAL(1, "相等"),


    /**
     * 当传入鉴权code和数据库相应字段按位&大于0时
     */
    BOOLEAN_AND(2, "按位与"),



    ;


    private final Integer code;

    private final String desc;


}
