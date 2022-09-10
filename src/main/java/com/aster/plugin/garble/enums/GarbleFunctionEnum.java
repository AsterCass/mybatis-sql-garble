package com.aster.plugin.garble.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author astercasc
 */
@Getter
@AllArgsConstructor
public enum GarbleFunctionEnum {


    /**
     * 获取更新数据，并处理
     */
    UPDATED_DATA_MSG(1, "更新数据回调", "updated-data-msg."),


    /**
     * 查询根据指定字段鉴权
     */
    SELECT_AUTHENTICATION(2, "查询鉴权", "auth.select."),

    /**
     * 插入授权
     */
    INSERT_AUTHENTICATION(3, "插入鉴权", "auth.insert."),

    /**
     * 查询根据指定字段鉴权
     */
    UPDATE_AUTHENTICATION(4, "更新鉴权", "auth.update."),


    ;


    private final Integer code;

    private final String desc;

    private final String propertyPre;

}
