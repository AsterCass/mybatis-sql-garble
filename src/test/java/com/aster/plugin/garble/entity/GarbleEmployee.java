package com.aster.plugin.garble.entity;

import lombok.Data;

import javax.persistence.Id;

@Data
public class GarbleEmployee {

    @Id
    private Long id;

    private Long cId;

    private String eName;

    private String eMsg;

    private Integer updateRecord;

}
