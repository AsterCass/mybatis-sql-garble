package com.aster.plugin.garble.mapper;

import com.aster.plugin.garble.entity.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    List<UserEntity> selectAll();

    List<String> selectAllPri();


    int updateOne(@Param("name") String name, @Param("ext") String ext);

    @Insert("insert into `garble`.`user` (id, `name`, ext) values (#{id},#{name},#{ext})")
    int insertOne(UserEntity userEntity);

    @Insert("INSERT into user (id,name,ext, update_record) VALUES (#{ida},'张老11','hhh', 0), (#{idb},'张老12','ggg',0)")
    int insertTwo(@Param("ida") int ida, @Param("idb") int idb);

}
