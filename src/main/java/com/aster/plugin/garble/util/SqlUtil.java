package com.aster.plugin.garble.util;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleRuntimeException;
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
                    tableName.add(fullName[fullName.length - 1].toLowerCase());
                }
            }
        }
        return tableName;
    }

    /**
     * 形如 garble.`user` 或者 user 是否包含和table包装相等
     */
    public static boolean garbleEqual(String tableString, GarbleTable table, String defaultScheme) {

        boolean equal = tableString.contains(GarbleTable.SCHEMA_SPLIT) &&
                tableString.replace(GarbleTable.TABLE_NAME_PROTECT, "")
                        .equals(table.getSimpleName());

        if (!equal && !tableString.contains(GarbleTable.SCHEMA_SPLIT) &&
                String.format("%s.%s", defaultScheme,
                                tableString.replace(GarbleTable.TABLE_NAME_PROTECT, ""))
                        .equals(table.getSimpleName())) {
            equal = true;
        }
        return equal;
    }

    /**
     * 形如 garble.`user` 或者 user 的集合是否包含和table包装相等 如果相等返回该字符串 否则返回 null
     */
    public static String garbleContain(List<String> tableStringList, GarbleTable table, String defaultScheme) {
        String resultString = null;
        for (String tableString : tableStringList) {
            if (garbleEqual(tableString, table, defaultScheme)) {
                resultString = tableString;
            }
        }
        return resultString;
    }


    /**
     * 从形如garble.`user`的全名中获取表名包装
     */
    public static GarbleTable getGarbleTableFromFullName(String defaultSchema, String fullTable) {
        String midFullTable = fullTable.replace("`", "");
        String[] fullName = midFullTable.split("\\.");
        if (0 != fullName.length) {
            GarbleTable garbleTable = new GarbleTable();
            garbleTable.setTableName(fullName[fullName.length - 1].toLowerCase());
            if (1 == fullName.length) {
                garbleTable.setSchemaName(defaultSchema.toLowerCase());
            } else {
                garbleTable.setSchemaName(fullName[0].toLowerCase());
            }
            return garbleTable;
        }
        throw new GarbleRuntimeException("table length is 0");
    }

    /**
     * 从形如garble.`user`的全名中获取表名包装
     */
    public static Set<GarbleTable> getGarbleTableFromFullName(String defaultSchema,
                                                              List<String> fullTableNames) {
        Set<GarbleTable> garbleTableList = new HashSet<>();
        if (null != fullTableNames && 0 != fullTableNames.size()) {
            for (String fullTable : fullTableNames) {
                String midFullTable = fullTable.replace("`", "");
                String[] fullName = midFullTable.split("\\.");
                if (0 != fullName.length) {
                    GarbleTable garbleTable = new GarbleTable();
                    garbleTable.setTableName(fullName[fullName.length - 1].toLowerCase());
                    if (1 == fullName.length) {
                        garbleTable.setSchemaName(defaultSchema.toLowerCase());
                    } else {
                        garbleTable.setSchemaName(fullName[0].toLowerCase());
                    }

                    garbleTableList.add(garbleTable);
                }
            }
        }
        return garbleTableList;
    }

    /**
     * 从形如garble.`user`的全名中获取表名包装
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
                    garbleTable.setTableName(fullName[fullName.length - 1].toLowerCase());
                    if (1 == fullName.length) {
                        garbleTable.setSchemaName(GarbleTable.getConnectSchema(ms).toLowerCase());
                    } else {
                        garbleTable.setSchemaName(fullName[0].toLowerCase());
                    }

                    garbleTableList.add(garbleTable);
                }
            }
        }
        return garbleTableList;
    }


}
