package com.aster.plugin.garble.mapper;

import com.aster.plugin.garble.entity.GarbleCompany;
import io.mybatis.mapper.Mapper;
import org.apache.ibatis.annotations.Update;

public interface CheckMapper extends Mapper<GarbleCompany, Long> {


    @Update("update garble_company set c_code = 111 where c_msg like '%x%' ")
    void updateAlone1();

    @Update("update `garble_company` set c_code = 222 where c_msg like '%x%' ")
    void updateAlone2();

    @Update("update garble.`garble_company` set c_code = 333 where c_msg like '%x%' ")
    void updateAlone3();

    @Update("update `garble`.`garble_company` set c_code = 444 where c_msg like '%x%' ")
    void updateAlone4();

    @Update("update `garBle`.`Garble_company` set c_code = 555 where c_msg like '%x%' ")
    void updateAlone5();

    @Update("update garble_company set c_code = 123 where id = 1")
    void updateAloneBack1();

    @Update("update garble_company set c_code = 234 where id = 2")
    void updateAloneBack2();

}
