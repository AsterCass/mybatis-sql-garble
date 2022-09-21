package com.aster.plugin.garble.interceptor;

import com.aster.plugin.garble.enums.AuthenticationTypeEnum;
import com.aster.plugin.garble.enums.GarbleFunctionEnum;
import com.aster.plugin.garble.property.AuthenticationFilterSelectProperty;
import com.aster.plugin.garble.service.SpecifiedMethodGenerator;
import com.aster.plugin.garble.util.PropertyUtil;
import com.aster.plugin.garble.work.AuthenticationFilterSelectAbstract;
import com.aster.plugin.garble.work.AuthenticationFilterSelectGarbleSql;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 功能
 * 1. 支持数据鉴权代码无感
 * @author astercasc
 */
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args =
                        {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args =
                        {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                                CacheKey.class, BoundSql.class})
        }
)
@Slf4j
public class GarbleQueryInterceptor implements Interceptor {

    /**
     * 传入配置
     */
    public AuthenticationFilterSelectProperty authenticationFilterSelectProperty;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (invocation.getArgs()[0] instanceof MappedStatement) {
            if (null != authenticationFilterSelectProperty) {
                authenticationFilterSelectProperty.setInvocation(invocation);
                AuthenticationFilterSelectAbstract garbleSql = new AuthenticationFilterSelectGarbleSql(
                        authenticationFilterSelectProperty);
                garbleSql.run();
            }
        }

        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        //ms.getConfiguration().getEnvironment().getDataSource().getConnection();
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();
        CacheKey cacheKey;
        BoundSql boundSql;
        //由于逻辑关系，只会进入一次
        if(args.length == 4){
            //4 个参数时
            boundSql = ms.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        } else {
            //6 个参数时
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

        log.info("[op:GarbleQueryInterceptor] setProperties start");
        //这里是兼容maven配置调用, spring boot 直接会调用 setAuthenticationFilterSelectProperty 方法
        if (null != properties) {
            Properties authenticationFilterSelectMap = new Properties();

            for (Object key : properties.keySet()) {
                if (key instanceof String &&
                        ((String) key).contains(GarbleFunctionEnum.SELECT_AUTHENTICATION.getPropertyPre())) {
                    authenticationFilterSelectMap.put(
                            ((String) key).replace(GarbleFunctionEnum.SELECT_AUTHENTICATION.getPropertyPre(),
                                    ""),
                            properties.get(key));
                }

            }
            if (0 != authenticationFilterSelectMap.size()) {
                setAuthenticationFilterSelectProperty(authenticationFilterSelectMap, null);
            }

        }
        log.info("[op:GarbleQueryInterceptor] setProperties end");
    }

    public void setAuthenticationFilterSelectProperty(Properties prop,
                                                      Map<Method, Object> springMethodForAuthCodeSelect) {
        log.info("[op:GarbleQueryInterceptor] setAuthenticationFilterSelectProperty start");
        this.authenticationFilterSelectProperty =
                PropertyUtil.propertyToObject(prop, AuthenticationFilterSelectProperty.class);
        if (null != authenticationFilterSelectProperty) {
            //spring 版本使用
            if (null != springMethodForAuthCodeSelect) {
                authenticationFilterSelectProperty.setMethodForAuthCodeSelect(springMethodForAuthCodeSelect);
            } else {
                List<Method> methodList = SpecifiedMethodGenerator.loadAuthCodeBySubTypes(
                        this.authenticationFilterSelectProperty.getAuthCodePath(),
                        AuthenticationTypeEnum.SELECT);
                if (0 != methodList.size()) {
                    Map<Method, Object> methodForAuthCodeSelect = new HashMap<>();
                    for (Method method : methodList) {
                        try {
                            methodForAuthCodeSelect.put(method, method.getDeclaringClass()
                                    .getDeclaredConstructor().newInstance());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    authenticationFilterSelectProperty.setMethodForAuthCodeSelect(methodForAuthCodeSelect);
                }
            }
        }
        log.info("[op:GarbleQueryInterceptor] setAuthenticationFilterSelectProperty end");
    }


}
