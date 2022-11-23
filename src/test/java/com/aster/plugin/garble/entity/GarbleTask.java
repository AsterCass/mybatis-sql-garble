package com.aster.plugin.garble.entity;

import io.mybatis.provider.Entity;
import lombok.Data;

@Data
@Entity.Table("garble_task")
public class GarbleTask {

    @Entity.Column(id = true)
    private Long id;

    @Entity.Column("e_id")
    private Long eId;

    @Entity.Column("t_name")
    private String tName;

    @Entity.Column("update_record")
    private Integer updateRecord;

}
