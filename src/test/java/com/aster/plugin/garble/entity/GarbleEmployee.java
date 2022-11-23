package com.aster.plugin.garble.entity;

import io.mybatis.provider.Entity;
import lombok.Data;

@Data
@Entity.Table("garble_employee")
public class GarbleEmployee {

    @Entity.Column(id = true)
    private Long id;

    @Entity.Column("c_id")
    private Long cId;

    @Entity.Column("e_name")
    private String eName;

    @Entity.Column("e_msg")
    private String eMsg;

    @Entity.Column("update_record")
    private Integer updateRecord;

}
