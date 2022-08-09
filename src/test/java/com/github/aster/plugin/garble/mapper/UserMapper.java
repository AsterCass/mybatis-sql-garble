package com.github.aster.plugin.garble.mapper;

import com.github.aster.plugin.garble.entity.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    List<UserEntity> selectAll();


    int updateOne(@Param("name") String name, @Param("py") String py);

    @Insert("insert into user (id, `name`, py) values (#{id},#{name},#{py})")
    int insertOne(UserEntity userEntity);

}
