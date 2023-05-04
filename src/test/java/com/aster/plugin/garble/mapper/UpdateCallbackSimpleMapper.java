package com.aster.plugin.garble.mapper;

import org.apache.ibatis.annotations.Update;

public interface UpdateCallbackSimpleMapper {


    @Update("update garble_company set c_code = 111 , c_msg= 'xxx' where c_msg like '%x%'")
    void updateAlone1();

    @Update("update `garble_company` set c_code = 222, update_record = 1 where c_msg like '%x%' ")
    void updateAlone2();

    @Update("update garble.`garble_company` set c_code = 333 where c_msg like '%x%' ")
    void updateAlone3();

    @Update("update `garble`.`garble_company` ggc set ggc.c_code = 444 where ggc.c_msg like '%x%' ")
    void updateAlone4();

    @Update("update `garBle`.`Garble_company` set c_code = 555 where c_msg like '%x%' ")
    void updateAlone5();

    @Update("update garble_task set t_name = '工作xx' where e_id = 22 ")
    void updateAlone6();

    @Update("update garble_company set c_code = 123, c_msg='bx' where id = 1")
    void updateAloneBack1();

    @Update("update garble_company set c_code = 234, c_msg='tx' where id = 2")
    void updateAloneBack2();

    @Update("update garble_task set t_name = '工作4' where id = 4")
    void updateAloneBack3();

    @Update("update garble_task set t_name = '工作5' where id = 5")
    void updateAloneBack4();

}
