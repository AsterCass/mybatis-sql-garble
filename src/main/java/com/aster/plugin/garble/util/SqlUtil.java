package com.aster.plugin.garble.util;

import java.util.ArrayList;
import java.util.List;

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
}
