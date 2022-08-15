package com.github.aster.plugin.garble.mapper;

import com.github.aster.plugin.garble.entity.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    List<UserEntity> selectAll();

    List<String> selectAllPri();


    int updateOne(@Param("name") String name, @Param("ext") String ext);

    @Insert("insert into user (id, `name`, ext) values (#{id},#{name},#{ext})")
    int insertOne(UserEntity userEntity);

}
