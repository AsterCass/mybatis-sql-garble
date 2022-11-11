package com.aster.plugin.garble.sql;


import com.aster.plugin.garble.bean.GarbleTable;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.ArrayList;
import java.util.List;

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
    public List<String> getTableList(String sql) {
        return new ArrayList<>();
    }

    /**
     * 获取所有表名包装
     *
     * @param sql sql
     * @return 复杂结构表名 包括schema
     */
    public List<GarbleTable> getGarbleTableList(MappedStatement ms, String sql) {
        return new ArrayList<>();
    }

}
