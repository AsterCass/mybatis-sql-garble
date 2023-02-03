package com.aster.plugin.garble.mapper;

import com.aster.plugin.garble.entity.GarbleEmployee;
import com.aster.plugin.garble.entity.GarbleTask;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AuthSelectOtherMapper {

    @Select("select * from garble_employee ge")
    List<GarbleEmployee> selectAll();

    @Select("select * from garble_task join garble_employee on garble_task.e_id = garble_employee.id")
    List<GarbleTask> selectAllTask();

    @Select("select * from garble_else.garble_task gt join garble_employee ge on gt.e_id = concat(ge.id,'0')")
    List<GarbleTask> selectAllElseTask();

    @Select("select * from garble_employee join garble_task on garble_task.e_id = garble_employee.id")
    List<GarbleEmployee> selectAllTaskRe();

    @Select("select * from `garble_employee` ge join garble_else.garble_task gt on gt.e_id = concat(ge.id,'0')")
    List<GarbleEmployee> selectAllElseTaskRe();

    @Select("select * from `garble_else`.`garble_task` gt where left(gt.e_id,2) in (select id from `garble_employee` ge)")
    List<GarbleTask> selectChildElseTask();

    @Select("select * from garble.garble_employee ge where " +
            "concat(ge.id,'0') in (select e_id from `garble_else`.`garble_task` gt)")
    List<GarbleEmployee> selectChildElseTaskRe();

}
