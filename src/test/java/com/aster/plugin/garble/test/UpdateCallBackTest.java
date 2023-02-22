package com.aster.plugin.garble.test;


import com.aster.plugin.garble.entity.GarbleCompany;
import com.aster.plugin.garble.entity.GarbleTask;
import com.aster.plugin.garble.mapper.CompanyMapper;
import com.aster.plugin.garble.mapper.TaskMapper;
import com.aster.plugin.garble.mapper.UpdateCallbackOtherMapper;
import com.aster.plugin.garble.mapper.UpdateCallbackSimpleMapper;
import com.aster.plugin.garble.util.MybatisHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

@Slf4j
public class UpdateCallBackTest {

    @Test
    public void updateRowCallBackSimple() {
        log.info("[op:updateRowCallBackSimple] start");
        SqlSession sqlSession = MybatisHelper.getUpdateCallbackSimpleSession();
        UpdateCallbackSimpleMapper mapper = sqlSession.getMapper(UpdateCallbackSimpleMapper.class);
        CompanyMapper companyMapper = sqlSession.getMapper(CompanyMapper.class);
        TaskMapper taskMapper = sqlSession.getMapper(TaskMapper.class);
        log.info("[op:updateRowCallBackSimple] update 1 have call back");
        mapper.updateAlone1();
        sqlSession.commit();
        Optional<GarbleCompany> company1 = companyMapper.selectByPrimaryKey(1L);
        if (company1.isPresent()) {
            Long code = company1.get().getCCode();
            Assert.assertEquals(111, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 2 have call back");
        mapper.updateAlone2();
        sqlSession.commit();
        Optional<GarbleCompany> company2 = companyMapper.selectByPrimaryKey(2L);
        if (company2.isPresent()) {
            Long code = company2.get().getCCode();
            Assert.assertEquals(222, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 3 have call back");
        mapper.updateAlone3();
        sqlSession.commit();
        Optional<GarbleCompany> company3 = companyMapper.selectByPrimaryKey(1L);
        if (company3.isPresent()) {
            Long code = company3.get().getCCode();
            Assert.assertEquals(333, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 4 have call back");
        mapper.updateAlone4();
        sqlSession.commit();
        Optional<GarbleCompany> company4 = companyMapper.selectByPrimaryKey(2L);
        if (company4.isPresent()) {
            Long code = company4.get().getCCode();
            Assert.assertEquals(444, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 5 have call back");
        mapper.updateAlone5();
        sqlSession.commit();
        Optional<GarbleCompany> company5 = companyMapper.selectByPrimaryKey(1L);
        if (company5.isPresent()) {
            Long code = company5.get().getCCode();
            Assert.assertEquals(555, code.longValue());
        }
        //这里是回调函数验证正确性 不好写Assert 需要观察日志打印是否为
        // 1:{"garble_company":["1","2"]}
        // 2:{"garble_company":["1","2"]}

        log.info("[op:updateRowCallBackSimple] update 6 no call back");
        mapper.updateAlone6();
        sqlSession.commit();
        Optional<GarbleTask> task64 = taskMapper.selectByPrimaryKey(4L);
        Optional<GarbleTask> task65 = taskMapper.selectByPrimaryKey(5L);
        if (task64.isPresent() && task65.isPresent()) {
            String name64 = task64.get().getTName();
            String name65 = task65.get().getTName();
            Assert.assertEquals("工作xx", name64);
            Assert.assertEquals("工作xx", name65);
        }

        log.info("[op:updateRowCallBackSimple] data rollback");
        mapper.updateAloneBack1();
        mapper.updateAloneBack2();
        mapper.updateAloneBack3();
        mapper.updateAloneBack4();
        sqlSession.commit();

        sqlSession.close();
        log.info("[op:updateRowCallBackSimple] end");

    }

    @Test
    public void updateRowCallBackOther() {
        log.info("[op:updateRowCallBackOther] start");
        SqlSession sqlSession = MybatisHelper.getUpdateCallbackOtherSession();
        UpdateCallbackOtherMapper mapper = sqlSession.getMapper(UpdateCallbackOtherMapper.class);
        CompanyMapper companyMapper = sqlSession.getMapper(CompanyMapper.class);
        TaskMapper taskMapper = sqlSession.getMapper(TaskMapper.class);
        log.info("[op:updateRowCallBackOther] update 1 have call back");
        mapper.updateAlone1();
        sqlSession.commit();
        Optional<GarbleCompany> company1 = companyMapper.selectByPrimaryKey(1L);
        if (company1.isPresent()) {
            Long code = company1.get().getCCode();
            Assert.assertEquals(111, code.longValue());
        }
        log.info("[op:updateRowCallBackOther] update 2 have call back");
        mapper.updateAlone2();
        sqlSession.commit();
        Optional<GarbleCompany> company2 = companyMapper.selectByPrimaryKey(2L);
        if (company2.isPresent()) {
            Long code = company2.get().getCCode();
            Assert.assertEquals(222, code.longValue());
        }
        //这里是回调函数验证正确性 不好写Assert 需要观察日志打印是否为
        // 1:{"garble_company":["1","2"]}
        // 2:{"garble_company":["1","2"]}
        log.info("[op:updateRowCallBackOther] update 3 no call back");
        mapper.updateAlone3();
        sqlSession.commit();
        Optional<GarbleTask> task34 = taskMapper.selectByPrimaryKey(4L);
        Optional<GarbleTask> task35 = taskMapper.selectByPrimaryKey(5L);
        if (task34.isPresent() && task35.isPresent()) {
            String name34 = task34.get().getTName();
            String name35 = task35.get().getTName();
            Assert.assertEquals("工作x", name34);
            Assert.assertEquals("工作x", name35);
        }
        log.info("[op:updateRowCallBackOther] update 4 no call back");
        mapper.updateAlone4();
        sqlSession.commit();
        Optional<GarbleTask> task44 = taskMapper.selectByPrimaryKey(4L);
        Optional<GarbleTask> task45 = taskMapper.selectByPrimaryKey(5L);
        if (task44.isPresent() && task45.isPresent()) {
            String name44 = task44.get().getTName();
            String name45 = task45.get().getTName();
            Assert.assertEquals("工作xx", name44);
            Assert.assertEquals("工作xx", name45);
        }
        log.info("[op:updateRowCallBackOther] update 5 have call back");
        mapper.updateAlone5();
        sqlSession.commit();
        List<String> task5List = mapper.checkAlone5();
        Assert.assertEquals(2, task5List.size());
        for (String task : task5List) {
            Assert.assertEquals("工作xxx", task);
        }
        //这里是回调函数验证正确性 不好写Assert 需要观察日志打印是否为
        // 1:{"garble_else.garble_task":["4","5"]}
        // 2:{"garble_else.garble_task":["4","5"]}
        log.info("[op:updateRowCallBackOther] data rollback");

        mapper.updateAloneBack1();
        mapper.updateAloneBack2();
        mapper.updateAloneBack3();
        mapper.updateAloneBack4();
        mapper.updateAloneBack5();
        mapper.updateAloneBack6();
        sqlSession.commit();

        sqlSession.close();
        log.info("[op:updateRowCallBackOther] end");

    }


    @Test
    public void elseTest() {
        System.out.println("123");
    }


}
