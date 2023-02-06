package com.aster.plugin.garble.mapper;


import com.aster.plugin.garble.entity.GarbleTask;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AuthInsertMapper {


    @Insert("insert into garble_task(id, t_name, e_id) values (30, '工作30x', 3000)")
    void insertSimple();

    @Insert("insert into `garble_task`(id, t_name, e_id) " +
            "values (40, '工作40x', 4000), (50, '工作40x', 4000)")
    void insertSimples();

    @Insert("insert into `garble_else`.`garble_task` (id, t_name, e_id) values (30, '工作30y', 3000)")
    void insertOther();

    @Insert("insert into garble_else.garble_task(id, t_name, e_id) " +
            "values (40, '工作40y', 4000), (50, '工作50y', 5000)")
    void insertOthers();

    /**
     * get else task
     */
    @Select("select * from `garble_else`.`garble_task`")
    List<GarbleTask> getAllOtherTask();

    /**
     * call back
     */
    @Delete("delete from garble_task where id in (30, 40, 50)")
    void rollbackSimple();

    /**
     * call back
     */
    @Delete("delete from garble_else.garble_task where id in (30, 40, 50)")
    void rollbackOther();


}
