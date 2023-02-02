package com.aster.plugin.garble.mapper;


import org.apache.ibatis.annotations.Insert;

public interface AuthInsertMapper {


    @Insert("insert into garble_task(id, t_name, e_id) values (30, '工作30x', 3000)")
    void insertSimple();

    @Insert("insert into garble_task(id, t_name, e_id) " +
            "values (40, '工作40x', 4000), (50, '工作40x', 4000)")
    void insertSimples();

    @Insert("insert into garble_else.garble_task(id, t_name, e_id) values (30, '工作30y', 3000)")
    void insertOther();

    @Insert("insert into garble_else.garble_task(id, t_name, e_id) " +
            "values (40, '工作40y', 4000), (50, '工作50y', 5000)")
    void insertOthers();


}
