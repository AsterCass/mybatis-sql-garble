package com.github.aster.plugin.garble.work;

import com.alibaba.fastjson.JSON;
import com.github.aster.plugin.garble.mapper.MonitorExcludeMapper;
import com.github.aster.plugin.garble.mybatis.MybatisGarbleSessionFactory;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.SqlSession;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MonitoredDataRollback extends MonitoredWork {

    @Resource
    MonitorExcludeMapper monitorExcludeMapper;

    public MonitoredDataRollback() {
        super();
    }

    public MonitoredDataRollback(Invocation invocation, String updateFlagVolName,
                              List<String> monitoredTableList, String excludedMapperPath) {
        super(invocation, updateFlagVolName, monitoredTableList, excludedMapperPath);
    }



    @Override
    public String exec() {
        try {

//            Class<?> monitorExcludeMapperClass = MonitorExcludeMapper.class;
//            Object monitorObject = SpringUtil.getBean("monitorExcludeMapper");
//
//            //get updated rows
//            Method monitorMethod = monitorExcludeMapperClass.getDeclaredMethod (
//                    "selectUpdatedRecord", String.class, String.class);
//            String list = JSON.toJSONString(monitorMethod.invoke(
//                    monitorObject, table, MONITOR_ID_MAP.get(table)));

//            Class<?> monitorExcludeMapperClass = MonitorExcludeMapper.class;
//            Method[] monitorExcludeMapperMethods = monitorExcludeMapperClass.getDeclaredMethods();
//            List<Method> methodList = Arrays.stream(monitorExcludeMapperMethods)
//                    .filter(cell -> cell.getName().equals("selectUpdatedRecord")).collect(Collectors.toList());
//            if(0 != methodList.size()) {
//                methodList.get(0).invoke(, )
//            }
            SqlSession sqlSession = MybatisGarbleSessionFactory.getSqlSession();
            MonitorExcludeMapper monitorExcludeMapper = sqlSession.getMapper(MonitorExcludeMapper.class);
            List<String> ids = monitorExcludeMapper.selectUpdatedRecord(table, "id", updateFlagVolName);

            //List<String> ids = monitorExcludeMapper.selectUpdatedRecord(table, "id", updateFlagVolName);

            //monitorExcludeMapper.selectUpdatedRecord(table, "id", updateFlagVolName);


            //roll back
//            List<String> idList = JSON.parseArray(list, String.class);
//            if(!CollectionUtils.isEmpty(idList)) {
//                Method rollBackMethod = monitorExcludeMapperClass.getMethod(
//                        "rollBackUpdatedRecord", String.class, String.class, List.class);
//                rollBackMethod.invoke(monitorObject, table, MONITOR_ID_MAP.get(table), idList);
//            }
//
//
//            log.info("[op:getUpdatedMonitorRows] update pri = {}", list);

            return "abc";
        } catch (Exception ex) {
//            log.error("[op:getUpdatedMonitorRows] ex = " + ex.getMessage());
//            log.error("[op:getUpdatedMonitorRows] exInfo = {}", JSON.toJSONString(ex.getStackTrace()));
            ex.printStackTrace();
            return "";
        }
    }
}
