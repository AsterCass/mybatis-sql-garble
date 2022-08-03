package com.github.aster.plugin.garble.test;

import com.alibaba.fastjson.JSON;
import com.github.aster.plugin.garble.entity.UserEntity;
import com.github.aster.plugin.garble.mapper.UserMapper;
import com.github.aster.plugin.garble.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

public class BaseTest {

    @Test
    public void baseTest() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
            List<UserEntity> list = userMapper.selectAll();
            System.out.println(JSON.toJSONString(list.get(0)));
        } finally {
            sqlSession.close();
        }

    }
}
