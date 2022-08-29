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


    ;


    private final Integer code;

    private final String desc;

    private final String propertyPre;

}
