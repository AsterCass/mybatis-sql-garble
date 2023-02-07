package com.aster.plugin.garble.mapper;

import com.aster.plugin.garble.entity.GarbleTask;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface AuthUpdateSimpleMapper {

    @SuppressWarnings("all")
    @Update("update garble_task set update_record = 1")
    void updateAll();

    @Update("update garble_task set update_record = 1 where e_id = 77")
    void updateEmployee();

    @Update("update garble_task gt set update_record = 1 where e_id = 77")
    void updateEmployeeAlias();

    @Update("update `garble`.garble_task set update_record = 1 where e_id = 77")
    void updateEmployeeSchema();

    @Update("update `garble`.`garble_task` gt set update_record = 1 where e_id = 77")
    void updateEmployeeSchemaAlias();






    @SuppressWarnings("all")
    @Update("update garble_task set update_record = 0")
    void updateCallBack();

    @Select("select * from garble_task where update_record = 1")
    List<GarbleTask> selectUpdatedTask();


}
