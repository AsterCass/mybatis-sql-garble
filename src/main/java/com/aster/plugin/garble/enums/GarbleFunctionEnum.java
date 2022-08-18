package com.aster.plugin.garble.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GarbleFunctionEnum {


    /**
     * 获取更新数据，并处理
     */
    GET_UPDATED_DATA(1, "获取更新数据"),


    /**
     * 查询根据指定字段鉴权
     */
    SELECT_AUTHENTICATION(2, "查询鉴权"),



    ;


    private final Integer code;

    private final String desc;

}
