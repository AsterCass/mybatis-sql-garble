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

    /**
     * 交集鉴权, 当传入鉴权List和数据库对于jsonString有交集时
     * todo 目前不支持数据中叉叉jsonString, 只支持数据中存储String
     */
    INTERSECTION(3, "交集"),



    ;


    private final Integer code;

    private final String desc;


}
