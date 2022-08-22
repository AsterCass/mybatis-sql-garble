package com.aster.plugin.garble.test;

import com.alibaba.fastjson.JSON;
import com.aster.plugin.garble.entity.UserEntity;
import com.aster.plugin.garble.mapper.UserMapper;
import com.aster.plugin.garble.sql.UpdateSqlCube;
import com.aster.plugin.garble.util.MybatisHelper;
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
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseTest {

    @Test
    public void queryBase() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
            List<UserEntity> list = userMapper.selectAll();
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
            userMapper.updateOne("张老二", "bbb");
            sqlSession.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testJSql() throws JSQLParserException {
        String sql = "        update ssd.hr_house_pr hhp,viw_summary c, ppp.hr_room\n" +
                "        set hhp.contact_name = c.name,hhp.contact_phone = c.phone, hr_room.vol=0\n" +
                "        where hhp.id = c.id and (hhp.contact_name is null or hhp.contact_name ='');";
        Statement statement = CCJSqlParserUtil.parse(sql);
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

        Map<String, String> monitoredTableMap = new HashMap<String, String>() {{
            put("hr_house_pr", "id");
            put("hr_house", "id");
            put("hr_address", "id");
            put("hr_room", "id");
        }};

        Map<String, String> monitoredTableUpdateFlagColMap = new HashMap<String, String>() {{
            put("hr_house_pr", "test");
        }};

        List<String> tableList = new ArrayList<>(monitoredTableMap.keySet());


        if (null != updateStatement.getUpdateSets() && 0 != updateStatement.getUpdateSets().size()) {
            List<UpdateSet> updateVol = new ArrayList<>();
            for (UpdateSet updateSet : updateStatement.getUpdateSets()) {
                for (int count = 0; count < updateSet.getColumns().size(); ++count) {
                    String name = updateSet.getColumns().get(count).getTable().getName();
                    String fullTableName = nameAliasMap.get(name);
                    if (tableList.contains(fullTableName)) {
                        String updateFlagVolName = monitoredTableUpdateFlagColMap.get(fullTableName);
                        updateFlagVolName = null == updateFlagVolName ? "update_record" : updateFlagVolName;

                        updateVol.add(new UpdateSet(
                                new Column(new Table(name), updateFlagVolName),
                                new LongValue(1)));
                        //防止由于多字段更新，导致的多次更新标志位
                        tableList.remove(fullTableName);
                    }
                }
            }
            updateStatement.getUpdateSets().addAll(updateVol);
        }


        UpdateSqlCube.getUpdateTableList(sql);

        System.out.println(sql);

    }

    @Test
    public void insert() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
            //这里的测试并不能获得正确的测试结果，因为session没有commit到sql内，所以无法获取更新个数
            userMapper.insertOne(new UserEntity(123, "张先生", "zzz"));
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
                "VALUES (8,'张老八','hhh', 0), (7,'张老七','ggg',0)";

        String insertSql2 = "INSERT into user (id,name,ext) VALUES (9,'张老九','iii')";


        String insertSql3 = "INSERT into user (id,name,ext) " +
                "VALUES (8,'张老八','hhh'), (7,'张老七','ggg')";

        String insertSql4 = "INSERT into user (id,name,ext, update_record) VALUES (9,'张老九','iii', 0)";


        Insert insert = (Insert) CCJSqlParserUtil.parse(insertSql4);

        boolean containFlag = false;
        for (int count = 0; count < insert.getColumns().size(); ++count) {
            if (insert.getColumns().get(count).getColumnName().equals("update_record")) {
                containFlag = true;
                ExpressionList exp = insert.getItemsList(ExpressionList.class);
                List<Expression> expressionList = exp.getExpressions();
                //兼容普通插入和List插入
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
            //兼容普通插入和List插入
            if (expressionList.get(0) instanceof RowConstructor) {
                for (Expression expression : expressionList) {
                    RowConstructor rowCon = (RowConstructor) expression;
                    rowCon.getExprList().addExpressions(new LongValue(1));
                }
            } else {
                expressionList.add(new LongValue(1));
            }
        }


        System.out.println(insert.toString());


    }

    @Test
    public void elseTest() {

    }
}
