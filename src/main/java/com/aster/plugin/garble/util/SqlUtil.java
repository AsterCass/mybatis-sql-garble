package com.aster.plugin.garble.util;

import com.aster.plugin.garble.bean.GarbleTable;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author astercasc
 */
public class SqlUtil {

    /**
     * 从形如garble.`user`的全民中获取表名user
     */
    public static List<String> getTableNameFromFullName(List<String> fullTableNames) {
        List<String> tableName = new ArrayList<>();
        if (null != fullTableNames && 0 != fullTableNames.size()) {
            for (String fullTable : fullTableNames) {
                String midFullTable = fullTable.replace("`", "");
                String[] fullName = midFullTable.split("\\.");
                if (0 != fullName.length) {
                    tableName.add(fullName[fullName.length - 1]);
                }
            }
        }
        return tableName;
    }

    /**
     * 从形如garble.`user`的全民中获取表名user
     */
    public static Set<GarbleTable> getGarbleTableFromFullName(MappedStatement ms,
                                                              List<String> fullTableNames) {
        Set<GarbleTable> garbleTableList = new HashSet<>();
        if (null != fullTableNames && 0 != fullTableNames.size()) {
            for (String fullTable : fullTableNames) {
                String midFullTable = fullTable.replace("`", "");
                String[] fullName = midFullTable.split("\\.");
                if (0 != fullName.length) {
                    GarbleTable garbleTable = new GarbleTable();
                    garbleTable.setTableName(fullName[fullName.length - 1]);
                    if (1 == fullName.length) {
                        garbleTable.setSchemaName(GarbleTable.getConnectSchema(ms));
                    } else {
                        garbleTable.setSchemaName(fullName[1]);
                    }

                    garbleTableList.add(garbleTable);
                }
            }
        }
        return garbleTableList;
    }


}
