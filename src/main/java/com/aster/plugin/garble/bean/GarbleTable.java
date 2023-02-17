package com.aster.plugin.garble.bean;

import com.aster.plugin.garble.exception.GarbleRuntimeException;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.schema.Table;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.Connection;

/**
 * @author astercasc
 */
@Data
@NoArgsConstructor
public class GarbleTable {

    private Table table;

    private String tableName;

    private String schemaName;

    private String aliasName;

    public static final String SCHEMA_SPLIT = ".";

    public static final String TABLE_NAME_PROTECT = "`";

    public String getFullName() {
        return String.format("`%s`.`%s`", this.schemaName, this.tableName);
    }

    public String getSimpleName() {
        return String.format("%s.%s", this.schemaName, this.tableName);
    }

    public void setTableName(String tableName) {
        if (null != tableName) {
            this.tableName = tableName.replace("`", "").toLowerCase();
        }
    }

    public void setSchemaName(String schemaName) {
        if (null != schemaName) {
            this.schemaName = schemaName.replace("`", "").toLowerCase();
        }
    }


    public boolean equal(GarbleTable other) {
        return this.getTableName().equals(other.getTableName())
                && this.getSchemaName().equals(other.getSchemaName());
    }

    public GarbleTable(String tableName, String schemaName, String defaultSchema) {
        this.setTableName(tableName.replace("`", "").toLowerCase());
        if (null == schemaName) {
            this.setSchemaName(defaultSchema.replace("`", "").toLowerCase());
        } else {
            this.setSchemaName(schemaName.replace("`", "").toLowerCase());
        }
    }

    public GarbleTable(Table table, String tableName, String schemaName, String aliasName) {
        this.table = table;
        this.aliasName = aliasName;
        this.setTableName(tableName);
        this.setSchemaName(schemaName);
    }

    public static String getConnectSchema(MappedStatement ms) {
        String schemaName;
        try {
            Connection con = ms.getConfiguration().getEnvironment().getDataSource().getConnection();
            if (null != con.getSchema() && 0 != con.getSchema().length()) {
                schemaName = con.getSchema();
            } else if (null != con.getCatalog() && 0 != con.getCatalog().length()) {
                schemaName = con.getCatalog();
            } else {
                throw new GarbleRuntimeException("connect schema get fail");
            }
        } catch (Exception ex) {
            throw new GarbleRuntimeException("无法获取数据库连接");
        }
        return schemaName;
    }

}
