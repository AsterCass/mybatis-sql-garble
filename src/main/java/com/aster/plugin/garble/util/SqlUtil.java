package com.aster.plugin.garble.util;

import com.aster.plugin.garble.bean.GarbleTable;
import com.aster.plugin.garble.exception.GarbleParamException;
import com.aster.plugin.garble.exception.GarbleRuntimeException;
import net.sf.jsqlparser.Model;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.Update;
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

    /**
     * 获取本层的sql中包含的tale
     */
    public static List<GarbleTable> getTableNameMapInSqlBody(Model stateModel, String defaultSchema) {

        List<GarbleTable> garbleTableList = new ArrayList<>();
        Table priTable;
        List<Join> joins;
        //table and join's 赋值
        if (stateModel instanceof PlainSelect) {
            FromItem fromItem = ((PlainSelect) stateModel).getFromItem();
            joins = ((PlainSelect) stateModel).getJoins();
            if (fromItem instanceof Table) {
                priTable = (Table) fromItem;
            } else {
                throw new GarbleParamException("查询语句FormItem解析失败");
            }

        } else if (stateModel instanceof Update) {
            Update update = (Update) stateModel;
            priTable = update.getTable();
            joins = ((Update) stateModel).getJoins();
        } else {
            throw new GarbleParamException("解析Sql当前层级类型接鉴别错误");
        }

        //pri table
        garbleTableList.add(getGarbleTableFromTable(priTable, defaultSchema));

        //join tables
        if (null != joins && 0 != joins.size()) {
            for (Join join : joins) {
                if (join.getRightItem() instanceof Table) {
                    Table joinTable = (Table) join.getRightItem();
                    garbleTableList.add(getGarbleTableFromTable(joinTable, defaultSchema));
                } else {
                    throw new GarbleParamException("查询语句JoinFormItem解析失败");
                }
            }
        }
        return garbleTableList;
    }

    private static GarbleTable getGarbleTableFromTable(Table table, String defaultSchema) {
        String schema = null == table.getSchemaName() ? defaultSchema : table.getSchemaName();
        if (null == table.getAlias()) {
            return new GarbleTable(table, table.getName(), schema, null);
        } else {
            return new GarbleTable(table, table.getName(), schema, table.getAlias().getName());
        }
    }


}
