package com.aster.plugin.garble.service;

import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
public interface DealWithUpdatedInterface {

    /**
     * 回调方法需要是实现DealWithUpdatedInterface, 并且需要通过@DealWithUpdated注解
     * 标明优先级, 如果存在多个继承DealWithUpdatedInterface的类，优先级priority更小的会更先执行
     * @param updatedTableMap 更新数据
     */
    void execute(Map<String, List<String>> updatedTableMap);

}
