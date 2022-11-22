package com.aster.plugin.garble.entity;

import io.mybatis.provider.Entity;
import lombok.Data;

@Data
@Entity.Table("garble_company")
public class GarbleCompany {


    @Entity.Column(id = true)
    private Long id;

    @Entity.Column("c_code")
    private Long cCode;

    @Entity.Column("c_name")
    private String cName;

    @Entity.Column("c_msg")
    private String cMsg;

    @Entity.Column("update_record")
    private Integer updateRecord;

}
