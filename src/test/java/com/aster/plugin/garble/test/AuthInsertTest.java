package com.aster.plugin.garble.test;

import com.aster.plugin.garble.entity.GarbleTask;
import com.aster.plugin.garble.mapper.AuthInsertMapper;
import com.aster.plugin.garble.mapper.TaskMapper;
import com.aster.plugin.garble.util.MybatisHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@Slf4j
public class AuthInsertTest {

    @Test
    public void authInsert() {
        log.info("[op:authInsert] start");
        //基本查询sql验证
        SqlSession sqlSession = MybatisHelper.getAuthInsertSession();
        TaskMapper mapper = sqlSession.getMapper(TaskMapper.class);
        List<GarbleTask> bxSTask = mapper.selectByExample(null);
        Assert.assertNotNull(bxSTask);
        Assert.assertEquals(bxSTask.size(), 0);
        //插入本schema数据库
        AuthInsertMapper insertMapper = sqlSession.getMapper(AuthInsertMapper.class);
        insertMapper.insertSimple();

        log.info("[op:authInsert] end");
    }

    @Test
    public void elseTest() {
        System.out.println("123");
    }
}
