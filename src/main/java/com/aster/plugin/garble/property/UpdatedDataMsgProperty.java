package com.aster.plugin.garble.property;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author astercasc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UpdatedDataMsgProperty extends MybatisRuntimeProperty {

    /**
     * 标记实现DealWithUpdatedInterface接口的方法路径，加快加快初始化速度，可以不赋值
     */
    protected String dealWithUpdatedPath;

    /**
     * 监控表和监控返回字段的Map，一般为主键，("user", "id")
     */
    protected Map<String, String> monitoredTableMap;

    /**
     * 监控表和更新标记字段Map ("user", "update_record")
     */
    protected Map<String, String> monitoredTableUpdateFlagColMap;

    /**
     * 默认更新标记字段，如果监控表无法在更新标记字段Map中取得，则会使用默认更新标记字段
     */
    protected String defaultFlagColName;

    /**
     * 不拦截的sql的路径
     */
    protected List<String> excludedMapperPath;


    /**
     * 继承 DealWithUpdatedInterface 的方法，用于做返回更新行的后续处理
     */
    protected Map<Method, Object> postMethodForUpdatedRows;


    public UpdatedDataMsgProperty(Invocation invocation) {
        super(invocation);
    }

}
