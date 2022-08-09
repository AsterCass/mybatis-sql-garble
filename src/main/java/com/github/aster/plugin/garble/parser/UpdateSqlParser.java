package com.github.aster.plugin.garble.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;

public class UpdateSqlParser {

    public List<String> getUpdateTable(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            Update updateStatement = (Update) statement;
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            return tablesNamesFinder.getTableList(updateStatement);
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        return new ArrayList<String>();
    }

}
