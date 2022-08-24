package com.aster.plugin.garble.sql;


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
}
