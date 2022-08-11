package com.github.aster.plugin.garble.test;

import com.alibaba.fastjson.JSON;
import com.github.aster.plugin.garble.mapper.UserMapper;
import com.github.aster.plugin.garble.util.MybatisHelper;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

public class BaseTest {

    @Test
    public void queryBase() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
            List<String> list = userMapper.selectAllPri();
            System.out.println(JSON.toJSONString(list.get(0)));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void updateBase() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
            //这里的测试并不能获得正确的测试结果，因为session没有commit到sql内，所以无法获取更新个数
            userMapper.updateOne("张老大", "ZLD");
            sqlSession.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testJSql() throws JSQLParserException {
        String sql = "        update hr_house_pr hhp,viw_summary c\n" +
                "        set hhp.contact_name = c.name,hr_house_pr.contact_phone = c.phone\n" +
                "        where hhp.id = c.id and (hhp.contact_name is null or hhp.contact_name ='');";
        Statement statement = CCJSqlParserUtil.parse(sql);
        Update update = (Update) statement;

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        System.out.println(tablesNamesFinder.getTableList(statement));

    }
}
