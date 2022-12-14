package com.aster.plugin.garble.bean;

import com.aster.plugin.garble.exception.GarbleRuntimeException;
import com.mysql.cj.jdbc.ConnectionImpl;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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

    public static String getConnectSchema(MappedStatement ms) {
        String schemaName = null;
        try {
            Connection con = ms.getConfiguration().getEnvironment().getDataSource().getConnection();
            if (con instanceof ConnectionImpl) {
                ConnectionImpl conImpl = (ConnectionImpl) con;
                schemaName = conImpl.getDatabase();
            }
            con.close();
        } catch (Exception ex) {
            throw new GarbleRuntimeException("无法获取数据库连接");
        }
        return schemaName;
    }

}
