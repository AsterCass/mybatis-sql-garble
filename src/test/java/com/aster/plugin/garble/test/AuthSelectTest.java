package com.aster.plugin.garble.test;


import com.aster.plugin.garble.entity.GarbleEmployee;
import com.aster.plugin.garble.entity.GarbleTask;
import com.aster.plugin.garble.mapper.AuthSelectSimpleMapper;
import com.aster.plugin.garble.mapper.EmployeeMapper;
import com.aster.plugin.garble.util.MybatisHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class AuthSelectTest {

    @Test
    public void authSelectSimple() {
        log.info("[op:authSelectSimple] start");
        //基本查询sql验证
        SqlSession sqlSession = MybatisHelper.getAuthSelectSimpleSession();
        EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
        List<GarbleEmployee> bxSEmployee = mapper.selectByExample(null);
        Assert.assertNotNull(bxSEmployee);
        Assert.assertEquals(bxSEmployee.size(), 4);
        for (GarbleEmployee employee : bxSEmployee) {
            Assert.assertTrue(Arrays.asList(11L, 22L, 33L, 44L).contains(employee.getId()));
        }
        //基本查询sql验证
        AuthSelectSimpleMapper simpleMapper = sqlSession.getMapper(AuthSelectSimpleMapper.class);
        List<GarbleEmployee> employees = simpleMapper.selectAll();
        sqlSession.commit();
        Assert.assertNotNull(employees);
        Assert.assertEquals(employees.size(), 4);
        for (GarbleEmployee employee : employees) {
            Assert.assertTrue(Arrays.asList(11L, 22L, 33L, 44L).contains(employee.getId()));
        }
        //单条件sql验证
        List<GarbleEmployee> employeesOneCondition = simpleMapper.selectOneCondition();
        sqlSession.commit();
        Assert.assertNotNull(employeesOneCondition);
        Assert.assertEquals(employeesOneCondition.size(), 1);
        Assert.assertEquals(33L, (long) employeesOneCondition.get(0).getId());
        //多条件sql验证
        List<GarbleEmployee> employeesMultiCondition = simpleMapper.selectMultiCondition();
        sqlSession.commit();
        Assert.assertNotNull(employeesMultiCondition);
        Assert.assertEquals(employeesMultiCondition.size(), 2);
        for (GarbleEmployee employee : employeesMultiCondition) {
            Assert.assertTrue(Arrays.asList(22L, 33L).contains(employee.getId()));
        }
        //别名sql验证
        List<GarbleEmployee> employeesOneConditionAlias = simpleMapper.selectAlias();
        sqlSession.commit();
        Assert.assertNotNull(employeesOneConditionAlias);
        Assert.assertEquals(employeesOneConditionAlias.size(), 1);
        Assert.assertEquals(33L, (long) employeesOneConditionAlias.get(0).getId());
        //单子查询验证
        List<GarbleEmployee> employeesOtherSub = simpleMapper.selectOtherSub();
        sqlSession.commit();
        Assert.assertNotNull(employeesOtherSub);
        Assert.assertEquals(employeesOtherSub.size(), 1);
        Assert.assertEquals(11L, (long) employeesOtherSub.get(0).getId());
        //单联表查询验证
        List<GarbleEmployee> employeesOtherJoin = simpleMapper.selectOtherJoin();
        sqlSession.commit();
        Assert.assertNotNull(employeesOtherJoin);
        Assert.assertEquals(employeesOtherJoin.size(), 2);
        for (GarbleEmployee employee : employeesOtherJoin) {
            Assert.assertTrue(Arrays.asList(11L, 33L).contains(employee.getId()));
        }
        //子查询验证
        List<GarbleTask> taskSub = simpleMapper.selectSub();
        sqlSession.commit();
        Assert.assertNotNull(taskSub);
        Assert.assertEquals(taskSub.size(), 6);
        for (GarbleTask task : taskSub) {
            Assert.assertTrue(Arrays.asList(1L, 2L, 3L ,4L, 5L, 6L).contains(task.getId()));
        }
        //被联表查询验证
        List<GarbleTask> taskJoin = simpleMapper.selectJoin();
        sqlSession.commit();
        Assert.assertNotNull(taskJoin);
        Assert.assertEquals(taskJoin.size(), 6);
        for (GarbleTask task : taskJoin) {
            Assert.assertTrue(Arrays.asList(1L, 2L, 3L ,4L, 5L, 6L).contains(task.getId()));
        }

        log.info("[op:authSelectSimple] base filter success");

    }


    @Test
    public void elseTest() {
        System.out.println("123");
    }


}
