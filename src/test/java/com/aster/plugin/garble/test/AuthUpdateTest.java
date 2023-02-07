package com.aster.plugin.garble.test;

import com.aster.plugin.garble.entity.GarbleTask;
import com.aster.plugin.garble.mapper.AuthUpdateSimpleMapper;
import com.aster.plugin.garble.mapper.TaskMapper;
import com.aster.plugin.garble.util.MybatisHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class AuthUpdateTest {

    @Test
    public void authUpdateSimple() {
        log.info("[op:authUpdateSimple] start");
        //基本查询sql验证
        SqlSession sqlSession = MybatisHelper.getAuthUpdateSimpleSession();
        TaskMapper mapper = sqlSession.getMapper(TaskMapper.class);
        List<GarbleTask> bsTask = mapper.selectByExample(null);
        Assert.assertNotNull(bsTask);
        Assert.assertEquals(bsTask.size(), 24);
        //session获取
        AuthUpdateSimpleMapper simpleMapper = sqlSession.getMapper(AuthUpdateSimpleMapper.class);
        //基本更新sql验证
        {
            simpleMapper.updateAll();
            sqlSession.commit();
            List<GarbleTask> taskList = simpleMapper.selectUpdatedTask();
            Assert.assertNotNull(taskList);
            Assert.assertEquals(taskList.size(), 3);
            for (GarbleTask garbleTask : taskList) {
                Assert.assertTrue(Arrays.asList(11L, 12L, 13L).contains(garbleTask.getId()));
            }
            simpleMapper.updateCallBack();
            sqlSession.commit();
        }
        //基本更新sql验证
        {
            simpleMapper.updateEmployee();
            sqlSession.commit();
            List<GarbleTask> taskList = simpleMapper.selectUpdatedTask();
            Assert.assertNotNull(taskList);
            Assert.assertEquals(taskList.size(), 2);
            for (GarbleTask task : taskList) {
                Assert.assertTrue(Arrays.asList(11L, 12L).contains(task.getId()));
            }
            simpleMapper.updateCallBack();
            sqlSession.commit();
        }

        sqlSession.close();
        log.info("[op:authUpdateSimple] end");

    }
}
