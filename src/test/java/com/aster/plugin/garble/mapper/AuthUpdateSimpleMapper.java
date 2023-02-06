package com.aster.plugin.garble.mapper;

import org.apache.ibatis.annotations.Update;

public interface AuthUpdateSimpleMapper {

    @SuppressWarnings("all")
    @Update("update garble_task set update_record = 1")
    void updateAll();





    @SuppressWarnings("all")
    @Update("update garble_task set update_record = 0")
    void updateCallBack();


}
