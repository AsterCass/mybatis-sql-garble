package com.aster.plugin.garble.test;


import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.entity.GarbleCompany;
import com.aster.plugin.garble.mapper.CheckMapper;
import com.aster.plugin.garble.sql.UpdateSqlCube;
import com.aster.plugin.garble.util.MybatisHelper;
import com.aster.plugin.garble.util.SqlUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class CheckTest {

    @Test
    public void updateRowCallBackSimple() {
        log.info("[op:updateRowCallBackSimple] start");
        SqlSession sqlSession = MybatisHelper.getUpdateRollbackSimpleSession();
        CheckMapper mapper = sqlSession.getMapper(CheckMapper.class);
        log.info("[op:updateRowCallBackSimple] update 1");
        mapper.updateAlone1();
        sqlSession.commit();
        Optional<GarbleCompany> company1 = mapper.selectByPrimaryKey(1L);
        if (company1.isPresent()) {
            Long code = company1.get().getCCode();
            Assert.assertEquals(111, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 2");
        mapper.updateAlone2();
        sqlSession.commit();
        Optional<GarbleCompany> company2 = mapper.selectByPrimaryKey(2L);
        if (company2.isPresent()) {
            Long code = company2.get().getCCode();
            Assert.assertEquals(222, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 3");
        mapper.updateAlone3();
        sqlSession.commit();
        Optional<GarbleCompany> company3 = mapper.selectByPrimaryKey(1L);
        if (company3.isPresent()) {
            Long code = company3.get().getCCode();
            Assert.assertEquals(333, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 4");
        mapper.updateAlone4();
        sqlSession.commit();
        Optional<GarbleCompany> company4 = mapper.selectByPrimaryKey(2L);
        if (company4.isPresent()) {
            Long code = company4.get().getCCode();
            Assert.assertEquals(444, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 5");
        mapper.updateAlone5();
        sqlSession.commit();
        Optional<GarbleCompany> company5 = mapper.selectByPrimaryKey(1L);
        if (company5.isPresent()) {
            Long code = company5.get().getCCode();
            Assert.assertEquals(555, code.longValue());
        }

        //这里是回调函数验证正确性 不好写Assert 需要观察日志打印是否为
        // 1:{"`garble`.`garble_company`":["1","2"]}
        // 2:{"`garble`.`garble_company`":["1","2"]}

        log.info("[op:updateRowCallBackSimple] data rollback");

        mapper.updateAloneBack1();
        mapper.updateAloneBack2();
        sqlSession.commit();
        log.info("[op:updateRowCallBackSimple] end");

    }

    @Test
    public void updateRowCallBackOther() {
        log.info("[op:updateRowCallBackSimple] start");
        SqlSession sqlSession = MybatisHelper.getUpdateRollbackOtherSession();
        CheckMapper mapper = sqlSession.getMapper(CheckMapper.class);
        log.info("[op:updateRowCallBackSimple] update 1");
        mapper.updateAlone1();
        sqlSession.commit();
        Optional<GarbleCompany> company1 = mapper.selectByPrimaryKey(1L);
        if (company1.isPresent()) {
            Long code = company1.get().getCCode();
            Assert.assertEquals(111, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 2");
        mapper.updateAlone2();
        sqlSession.commit();
        Optional<GarbleCompany> company2 = mapper.selectByPrimaryKey(2L);
        if (company2.isPresent()) {
            Long code = company2.get().getCCode();
            Assert.assertEquals(222, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 3");
        mapper.updateAlone3();
        sqlSession.commit();
        Optional<GarbleCompany> company3 = mapper.selectByPrimaryKey(1L);
        if (company3.isPresent()) {
            Long code = company3.get().getCCode();
            Assert.assertEquals(333, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 4");
        mapper.updateAlone4();
        sqlSession.commit();
        Optional<GarbleCompany> company4 = mapper.selectByPrimaryKey(2L);
        if (company4.isPresent()) {
            Long code = company4.get().getCCode();
            Assert.assertEquals(444, code.longValue());
        }
        log.info("[op:updateRowCallBackSimple] update 5");
        mapper.updateAlone5();
        sqlSession.commit();
        Optional<GarbleCompany> company5 = mapper.selectByPrimaryKey(1L);
        if (company5.isPresent()) {
            Long code = company5.get().getCCode();
            Assert.assertEquals(555, code.longValue());
        }

        //这里是回调函数验证正确性 不好写Assert 需要观察日志打印是否为
        // 1:{"`garble`.`garble_company`":["1","2"]}
        // 2:{"`garble`.`garble_company`":["1","2"]}

        log.info("[op:updateRowCallBackSimple] data rollback");

        mapper.updateAloneBack1();
        mapper.updateAloneBack2();
        sqlSession.commit();
        log.info("[op:updateRowCallBackSimple] end");

    }


    @Test
    public void elseTest() throws JSQLParserException {
        String updateTwoTable = "UPDATE a.product p INNER JOIN product_price ON p.productid= pp.productid " +
                "SET pp.price = p.price * 0.8, p.dateUpdate = CURDATE()";
        Statement statement = CCJSqlParserUtil.parse(updateTwoTable);
        Update updateStatement = (Update) statement;
        ArrayList<UpdateSet> sets = updateStatement.getUpdateSets();
        //getName方法只取表名不取schema名
        Table table = updateStatement.getTable();
        String schema = table.getSchemaName();
        String tableName = updateStatement.getTable().getName();
        Map<GarbleTable, String> nameAliasMap = UpdateSqlCube.getUpdateTableAliasMap(updateTwoTable, "garble");

        List<String> fullTableList = new TablesNamesFinder().getTableList(statement);
        Set<GarbleTable> tableSet = SqlUtil.getGarbleTableFromFullName("garble", fullTableList);


        System.out.println("123");
    }


}
