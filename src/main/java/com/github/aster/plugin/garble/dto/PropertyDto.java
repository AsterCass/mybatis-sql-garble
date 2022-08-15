package com.github.aster.plugin.garble.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PropertyDto {


    /**
     * 监控表和监控返回字段的Map，一般为主键，("user", "id")
     */
    Map<String, String> monitoredTableMap;

    /**
     * 监控表和更新标记字段Map ("user", "update_record")
     */
    Map<String, String> monitoredTableUpdateFlagColMap;

    /**
     * 默认更新标记字段，如果监控表无法在更新标记字段Map中取得，则会使用默认更新标记字段
     */
    String defaultFlagColName;

    /**
     * 不拦截的sql的路径
     */
    List<String> excludedMapperPath;




}
