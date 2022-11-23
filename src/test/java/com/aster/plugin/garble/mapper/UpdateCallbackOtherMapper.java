package com.aster.plugin.garble.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UpdateCallbackOtherMapper {


    @Update("update garble_company set c_code = 111 where c_msg like '%x%' ")
    void updateAlone1();

    @Update("update `garble`.`garble_company` set c_code = 222 where c_msg like '%x%' ")
    void updateAlone2();

    @Update("update `garble_task` set t_name = '工作x' where e_id = 22 ")
    void updateAlone3();

    @Update("update `garble`.`garble_task` set t_name = '工作xx' where e_id = 22 ")
    void updateAlone4();

    @Update("update `garble_else`.`garble_task` set t_name = '工作xxx' where e_id = 220 ")
    void updateAlone5();

    @Select("select t_name from `garble_else`.`garble_task` where e_id = 220 ")
    List<String> checkAlone5();


    @Update("update garble_company set c_code = 123 where id = 1")
    void updateAloneBack1();

    @Update("update garble_company set c_code = 234 where id = 2")
    void updateAloneBack2();

    @Update("update garble_task set t_name = '工作4' where id = 4")
    void updateAloneBack3();

    @Update("update garble_task set t_name = '工作5' where id = 5")
    void updateAloneBack4();

    @Update("update garble_else.garble_task set t_name = '工作4' where id = 4")
    void updateAloneBack5();

    @Update("update garble_else.garble_task set t_name = '工作5' where id = 5")
    void updateAloneBack6();


}
