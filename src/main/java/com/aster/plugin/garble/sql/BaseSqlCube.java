package com.aster.plugin.garble.sql;


import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.util.SqlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author astercasc
 */
public abstract class BaseSqlCube {

    /**
     * 获取所有表名
     *
     * @param sql sql
     * @return 简单表名，不包含schema
     */
    @Deprecated
    public List<String> getTableList(String sql) {
        return new ArrayList<>();
    }

    /**
     * 获取所有表名包装
     *
     * @param sql sql
     * @return 复杂结构表名 包括schema
     */
    public Set<GarbleTable> getGarbleTableList(MappedStatement ms, String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            List<String> fullTableList = new TablesNamesFinder().getTableList(statement);
            Set<GarbleTable> tableSet = SqlUtil.getGarbleTableFromFullName(ms, fullTableList);
            if (0 == tableSet.size()) {
                throw new GarbleParamException("查询语句Table解析失败" + sql);
            }
            return tableSet;
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return new HashSet<>();
    }

}
