package com.aster.plugin.garble.exception;

/**
 * @author astercasc
 */
public class GarbleRuntimeException extends RuntimeException {

    /**
     * 生成异常
     */
    public GarbleRuntimeException(String message) {
        super(message);
    }

    /**
     * 已抓异常抛出
     */
    public GarbleRuntimeException(Throwable cause) {
        super(cause);
    }


}
