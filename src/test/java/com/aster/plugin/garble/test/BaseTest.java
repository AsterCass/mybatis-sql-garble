package com.aster.plugin.garble.test;

import com.alibaba.fastjson.JSON;
import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.entity.UserEntity;
import com.aster.plugin.garble.mapper.UserMapper;
import com.aster.plugin.garble.property.UpdatedDataMsgProperty;
import com.aster.plugin.garble.sql.SelectAuthFilterSqlCube;
import com.aster.plugin.garble.sql.UpdateAuthFilterSqlCube;
import com.aster.plugin.garble.util.MybatisHelper;
import com.aster.plugin.garble.util.PropertyUtil;
import com.aster.plugin.garble.util.SqlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class BaseTest {

    @Test
    public void queryBase() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
            List<UserEntity> list = userMapper.selectAll();
            System.out.println(JSON.toJSONString(list));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void insertBase() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
//            UserEntity userEntity = new UserEntity();
//            userEntity.setId(777);
//            userEntity.setName("888");
//            userEntity.setExt("999");
//            userMapper.insertOne(userEntity);

            sqlSession.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            sqlSession.close();
        }
    }


    @Test
    public void updateBase() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
            //????????????????????????????????????????????????????????????session??????commit???sql????????????????????????????????????
            userMapper.updateOne("???????????????", "bbb");

            sqlSession.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void propertyTest() {
        Properties properties = new Properties();
        properties.put("defaultFlagColName", "update_record");
        properties.put("excludedMapperPath", "['abc','efg','hij']");
        properties.put("monitoredTableMap", "{'user':'id'}");
        UpdatedDataMsgProperty property = PropertyUtil.propertyToObject(properties, UpdatedDataMsgProperty.class);
        System.out.println(property);
    }

    @Test
    public void queryTest() throws JSQLParserException {


        String sql = "select * from sch1.user us join sch2.per pe on per.co1 = user.col2 " +
                "where us.update_record = 0 and pe.status = 0";
        String sql1 = "select us.id  from garble.`user` us join testTable on testTable.id = us.t_id where " +
                "testTable.code = us.code+1 or us.per_id in (select id from garble.per where ext < 50 and status = 0) and stat = 1";
        String sql2 = "select * from user, per where user.id = per.user_id";

        String sql3 = "select * from ping_ta where auth_code in (123, 34, 55323)";

        //======================== garbleTable test =========================

        Select select = (Select) CCJSqlParserUtil.parse(sql1);
        List<String> fullTableList = new TablesNamesFinder().getTableList(select);

        List<String> tableList = SqlUtil.getTableNameFromFullName(fullTableList);

        System.out.println(11111111);

        //==================================================================


        String newSql = new SelectAuthFilterSqlCube(
                Arrays.asList("user", "per", "testTable", "ping_ta"),
                new HashMap<String, String>() {{
                    put("user", "perCol");
                    put("per", "perCol");
                    put("testTable", "perCol");
                    put("ping_ta", "auth_code");
                }},

                new HashMap<String, Integer>() {{
                    put("user", 1);
                    put("per", 1);
                    put("testTable", 2);
                    put("ping_ta", 3);
                }},
                new HashMap<String, String>() {{
                    put("user", "123");
                    put("per", "123");
                    put("testTable", "7");
                    put("ping_ta", JSON.toJSONString(Arrays.asList("123", "456")));
                }}

        ).addAuthCode(sql3);


        System.out.println(newSql);
    }

    @Test
    public void testJSql() throws JSQLParserException {
//        String sql = "        update ssd.hr_house_pr hhp,viw_summary c, ppp.hr_room\n" +
//                "        set hhp.contact_name = c.name,hhp.contact_phone = c.phone, hr_room.vol=0\n" +
//                "        where hhp.id = c.id and (hhp.contact_name is null or hhp.contact_name ='');";

                String sql = "update sch.user set ext = '1' where id  in (select id from (select id from user where id = 1) tab)";

        Statement statement = CCJSqlParserUtil.parse(sql);
        List<String> fullTableList = new TablesNamesFinder().getTableList(statement);

        String authSql = new UpdateAuthFilterSqlCube(


                Arrays.asList("hr_house_pr", "viw_summary"),
                new HashMap<String, String>() {{
                    put("hr_house_pr", "house_col");
                    put("viw_summary", "sum_col");
                }},

                new HashMap<String, Integer>() {{
                    put("hr_house_pr", 1);
                    put("viw_summary", 1);
                }},

                new HashMap<String, String>() {{
                    put("hr_house_pr", "123");
                    put("viw_summary", "1234");
                }}

        ).addAuthCode(sql);




        Update updateStatement = (Update) statement;
        Map<String, String> nameAliasMap = new HashMap<>();
        if (null == updateStatement.getTable().getAlias()) {
            nameAliasMap.put(updateStatement.getTable().getName(),
                    updateStatement.getTable().getName());
        } else {
            nameAliasMap.put(updateStatement.getTable().getAlias().getName(),
                    updateStatement.getTable().getName());
        }
        if (null != updateStatement.getStartJoins() && 0 != updateStatement.getStartJoins().size()) {
            for (Join join : updateStatement.getStartJoins()) {
                Table rightTable = new Table();
                if (join.getRightItem() instanceof Table) {
                    rightTable = (Table) join.getRightItem();
                }
                if (null == rightTable.getAlias()) {
                    nameAliasMap.put(rightTable.getName(), rightTable.getName());
                } else {
                    nameAliasMap.put(rightTable.getAlias().getName(), rightTable.getName());
                }

            }
        }



        System.out.println(sql);

    }

    @Test
    public void insert() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
            //????????????????????????????????????????????????????????????session??????commit???sql????????????????????????????????????
            //userMapper.insertOne(new UserEntity(123, "?????????", "zzz"));
            //userMapper.insertTwo(123, 456);
            sqlSession.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            sqlSession.close();
        }


    }


    @Test
    public void insertTest() throws Exception {
        String insertSql1 = "INSERT into user (id,name,ext, update_record) " +
                "VALUES (8,'?????????','hhh', 0), (7,'?????????','ggg',0)";

        String insertSql2 = "INSERT into user (id,name,ext) VALUES (9,'?????????','iii')";


        String insertSql3 = "INSERT into user (id,name,ext) " +
                "VALUES (8,'?????????','hhh'), (7,'?????????','ggg')";

        String insertSql4 = "INSERT into user (id,name,ext, update_record) VALUES (9,'?????????','iii', 0)";

        String insertSql5 = "INSERT into `garble`.`user` (id,name,ext) VALUES (8,'?????????','hhh'), (7,'?????????','ggg')";


        Insert insert = (Insert) CCJSqlParserUtil.parse(insertSql4);

        //======================== garbleTable test =========================

        //mysql????????????????????????
        GarbleTable insertGarbleTable = new GarbleTable();
        insertGarbleTable.setTableName(insert.getTable().getName());
        insertGarbleTable.setSchemaName(insert.getTable().getSchemaName());
        if (null == insertGarbleTable.getSchemaName()) {
            //ms.getConfiguration().getEnvironment().getDataSource().getConnection();
            insertGarbleTable.setSchemaName("");
            //con.close
        }

        //=============================================================

        boolean containFlag = false;
        for (int count = 0; count < insert.getColumns().size(); ++count) {
            if (insert.getColumns().get(count).getColumnName().equals("update_record")) {
                containFlag = true;
                ExpressionList exp = insert.getItemsList(ExpressionList.class);
                List<Expression> expressionList = exp.getExpressions();
                //?????????????????????List??????
                if (expressionList.get(0) instanceof RowConstructor) {
                    for (Expression expression : expressionList) {
                        RowConstructor rowCon = (RowConstructor) expression;
                        rowCon.getExprList().getExpressions().set(count, new LongValue(1));
                    }
                } else {
                    expressionList.set(count, new LongValue(1));
                }
            }
        }
        if (!containFlag) {
            insert.addColumns(new Column("update_record"));
            ExpressionList exp = insert.getItemsList(ExpressionList.class);
            List<Expression> expressionList = exp.getExpressions();
            //?????????????????????List??????
            if (expressionList.get(0) instanceof RowConstructor) {
                for (Expression expression : expressionList) {
                    RowConstructor rowCon = (RowConstructor) expression;
                    rowCon.getExprList().addExpressions(new LongValue(1));
                }
            } else {
                expressionList.add(new LongValue(1));
            }
        }


        System.out.println(insert);


    }

    @Test
    public void elseTest() {

        Set<GarbleTable> test = new HashSet<>();
        GarbleTable t1 = new GarbleTable();
        t1.setSchemaName("111");
        t1.setTableName("111");
        GarbleTable t2 = new GarbleTable();
        t2.setSchemaName("111");
        t2.setTableName("111");
        test.add(t1);
        test.add(t2);

        System.out.println("111111");
    }
}
