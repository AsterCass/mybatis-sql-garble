package com.aster.plugin.garble.test;


import com.aster.plugin.garble.entity.GarbleEmployee;
import com.aster.plugin.garble.mapper.CompanyMapper;
import com.aster.plugin.garble.mapper.EmployeeMapper;
import com.aster.plugin.garble.mapper.TaskMapper;
import com.aster.plugin.garble.mapper.UpdateCallbackOtherMapper;
import com.aster.plugin.garble.util.MybatisHelper;
import io.mybatis.mapper.example.Example;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class AuthFunctionTest {

    @Test
    public void authSelectSimple() {
        log.info("[op:selectAuthSimple] start");
        SqlSession sqlSession = MybatisHelper.getAuthSelectSimpleSession();
        EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
        List<GarbleEmployee> bxSEmployee = mapper.selectByExample(null);
        Assert.assertNotNull(bxSEmployee);
        Assert.assertEquals(bxSEmployee.size(), 4);
        for (GarbleEmployee employee : bxSEmployee) {
            Assert.assertTrue(Arrays.asList(11L, 22L, 33L, 44L).contains(employee.getId()));
        }
        log.info("[op:selectAuthSimple] base filter success");

    }


    @Test
    public void elseTest() {
        System.out.println("123");
    }


}
