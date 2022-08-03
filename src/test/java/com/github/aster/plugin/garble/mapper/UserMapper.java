package com.github.aster.plugin.garble.mapper;

import com.github.aster.plugin.garble.entity.UserEntity;

import java.util.List;

public interface UserMapper {

    List<UserEntity> selectAll();

}
