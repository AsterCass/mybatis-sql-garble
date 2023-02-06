package com.aster.plugin.garble.test;

import com.aster.plugin.garble.entity.GarbleTask;
import com.aster.plugin.garble.mapper.AuthInsertMapper;
import com.aster.plugin.garble.mapper.TaskMapper;
import com.aster.plugin.garble.util.MybatisHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class AuthInsertTest {

    @Test
    public void authInsert() {
        log.info("[op:authInsert] start");
        //基本查询sql验证
        SqlSession sqlSession = MybatisHelper.getAuthInsertSession();
        TaskMapper mapper = sqlSession.getMapper(TaskMapper.class);
        AuthInsertMapper insertMapper = sqlSession.getMapper(AuthInsertMapper.class);
        List<GarbleTask> bsTask = mapper.selectByExample(null);
        Assert.assertNotNull(bsTask);
        Assert.assertEquals(bsTask.size(), 0);
        List<GarbleTask> bsETask = insertMapper.getAllOtherTask();
        Assert.assertNotNull(bsETask);
        Assert.assertEquals(bsETask.size(), 0);
        //插入本schema数据库
        {
            insertMapper.insertSimple();
            sqlSession.commit();
            List<GarbleTask> taskList = mapper.selectByExample(null);
            Assert.assertNotNull(taskList);
            Assert.assertEquals(taskList.size(), 1);
            for (GarbleTask task : taskList) {
                Assert.assertEquals(30L, (long) task.getId());
            }
            List<GarbleTask> otherTaskList = insertMapper.getAllOtherTask();
            Assert.assertNotNull(otherTaskList);
            Assert.assertEquals(otherTaskList.size(), 0);
        }
        //多条插入本schema数据库
        {
            insertMapper.insertSimples();
            sqlSession.commit();
            List<GarbleTask> taskList = mapper.selectByExample(null);
            Assert.assertNotNull(taskList);
            Assert.assertEquals(taskList.size(), 3);
            for (GarbleTask task : taskList) {
                Assert.assertTrue(Arrays.asList(30L, 40L, 50L).contains(task.getId()));
            }
            List<GarbleTask> otherTaskList = insertMapper.getAllOtherTask();
            Assert.assertNotNull(otherTaskList);
            Assert.assertEquals(otherTaskList.size(), 0);
        }
        //rollback simple
        {
            insertMapper.rollbackSimple();
            sqlSession.commit();
        }
        //单条插入其他schema数据库
        {
            insertMapper.insertOther();
            sqlSession.commit();
            List<GarbleTask> taskList = mapper.selectByExample(null);
            Assert.assertNotNull(taskList);
            Assert.assertEquals(taskList.size(), 0);
            List<GarbleTask> otherTaskList = insertMapper.getAllOtherTask();
            Assert.assertNotNull(otherTaskList);
            Assert.assertEquals(otherTaskList.size(), 1);
            for (GarbleTask task : otherTaskList) {
                Assert.assertEquals(30L, (long) task.getId());
            }
        }
        //多条插入其他schema数据库
        {
            insertMapper.insertOthers();
            sqlSession.commit();
            List<GarbleTask> taskList = mapper.selectByExample(null);
            Assert.assertNotNull(taskList);
            Assert.assertEquals(taskList.size(), 0);
            List<GarbleTask> otherTaskList = insertMapper.getAllOtherTask();
            Assert.assertNotNull(otherTaskList);
            Assert.assertEquals(otherTaskList.size(), 3);
            for (GarbleTask task : otherTaskList) {
                Assert.assertTrue(Arrays.asList(30L, 40L, 50L).contains(task.getId()));
            }
        }
        //rollback other
        {
            insertMapper.rollbackOther();
            sqlSession.commit();
        }

        log.info("[op:authInsert] end");
    }

    @Test
    public void elseTest() {
        System.out.println("123");
    }
}
