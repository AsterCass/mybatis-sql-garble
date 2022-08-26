package com.aster.plugin.garble.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.schema.Table;

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

}
