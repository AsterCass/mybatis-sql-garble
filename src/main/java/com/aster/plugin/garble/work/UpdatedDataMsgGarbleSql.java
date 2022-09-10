package com.aster.plugin.garble.work;

import com.aster.plugin.garble.property.UpdatedDataMsgProperty;
import com.aster.plugin.garble.sql.InsertSqlCube;
import com.aster.plugin.garble.sql.UpdateSqlCube;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.plugin.Invocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public class UpdatedDataMsgGarbleSql extends UpdatedDataMsgAbstract {


    public UpdatedDataMsgGarbleSql(Invocation invocation, UpdatedDataMsgProperty property) {
        super(invocation, property);
    }

    @Override
    public Map<String, List<String>> exec() {
        boolean isUpdate = false;
        boolean isInsert = false;
        try {
            Statement stat = CCJSqlParserUtil.parse(sql);
            if (stat instanceof Update) {
                isUpdate = true;
            } else if (stat instanceof Insert) {
                isInsert = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (isUpdate) {
            Map<String, String> monitoredTableUpdateColValueMap =
                    new HashMap<>();
            for (String table : crossTableList) {
                monitoredTableUpdateColValueMap.put(table, "1");
            }
            String newSql = UpdateSqlCube.addUpdateSet(sql, crossTableList,
                    monitoredTableUpdateFlagColMap, monitoredTableUpdateColValueMap);
            newSqlBuilder(newSql);
        } else if (isInsert) {
            InsertSqlCube.addInsertSet(invocation);
        }


        return new HashMap<>();
    }


}
