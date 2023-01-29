package com.aster.plugin.garble.mapper;

import com.aster.plugin.garble.entity.GarbleEmployee;
import com.aster.plugin.garble.entity.GarbleTask;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AuthSelectSimpleMapper {

    @Select("select * from garble_employee")
    List<GarbleEmployee> selectAll();

    @Select("select * from garble_employee where e_msg like '%s%'")
    List<GarbleEmployee> selectOneCondition();

    @Select("select * from garble_employee where e_msg like '%s%' or e_msg like '%q%'")
    List<GarbleEmployee> selectMultiCondition();

    @Select("select * from garble_employee ge where e_msg like '%s%'")
    List<GarbleEmployee> selectAlias();

    @Select("select * from garble_employee where (e_msg like '%z%' or e_msg like '%s%') and " +
            "id in (select e_id from garble_task where t_name like '工作1%')")
    List<GarbleEmployee> selectOtherSub();

    @Select("select ge.* from garble_employee ge join garble_task gt on ge.c_id = gt.id where " +
            "ge.e_msg like '%z%' or ge.e_msg like '%s%'")
    List<GarbleEmployee> selectOtherJoin();

    @Select("select * from garble_task where e_id in (select id from garble_employee ge)")
    List<GarbleTask> selectSub();

    @Select("select gt.* from garble_task gt join garble_employee ge on gt.e_id = ge.id")
    List<GarbleTask> selectJoin();

}
