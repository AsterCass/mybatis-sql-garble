package com.aster.plugin.garble.test;

import com.aster.plugin.garble.entity.GarbleEmployee;
import com.aster.plugin.garble.entity.GarbleTask;
import com.aster.plugin.garble.mapper.AuthSelectSimpleMapper;
import com.aster.plugin.garble.mapper.AuthUpdateSimpleMapper;
import com.aster.plugin.garble.mapper.TaskMapper;
import com.aster.plugin.garble.util.MybatisHelper;
import io.mybatis.mapper.example.Example;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.criteria.CriteriaBuilder;
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
        //基本查询sql验证
        {
            simpleMapper.updateAll();
            sqlSession.commit();
            Example<GarbleTask> taskExample = new Example<>();
            taskExample.createCriteria().andCondition("update_record = 1");
            List<GarbleTask> taskList = mapper.selectByExample(taskExample);
            //todo
            log.info("[op:authUpdateSimple] end");
            simpleMapper.updateCallBack();
            sqlSession.commit();

        }

        sqlSession.close();
        log.info("[op:authUpdateSimple] end");

    }
}
