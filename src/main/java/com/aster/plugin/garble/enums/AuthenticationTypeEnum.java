package com.aster.plugin.garble.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author astercasc
 */

@Getter
@AllArgsConstructor
public enum AuthenticationTypeEnum {


    /**
     * 更新鉴权
     */
    UPDATE(1, "更新鉴权"),

    /**
     * 查询鉴权
     */
    SELECT(2, "查询鉴权"),

    /**
     * 插入授权
     */
    INSERT(3, "插入授权"),



    ;


    private final Integer code;

    private final String desc;



}
