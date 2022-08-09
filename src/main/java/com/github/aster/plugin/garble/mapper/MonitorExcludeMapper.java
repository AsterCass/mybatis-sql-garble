package com.github.aster.plugin.garble.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MonitorExcludeMapper {


    List<String> selectUpdatedRecord(@Param(value = "table") String table,
                                     @Param(value = "pri") String pri,
                                     @Param(value = "updateFlagVolName") String updateFlagVolName);

    void rollBackUpdatedRecord(@Param(value = "table") String table,
                               @Param(value = "pri") String pri,
                               @Param(value = "ids") List<String> ids,
                               @Param(value = "updateFlagVolName") String updateFlagVolName);

}
