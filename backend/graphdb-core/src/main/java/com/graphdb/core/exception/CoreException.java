package com.graphdb.core.exception;

/**
 * 核心模块异常类
 */
public class CoreException extends RuntimeException {
    
    public CoreException(String message) {
        super(message);
    }
    
    public CoreException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CoreException(String errorCode, String message) {
        super(String.format("[%s] %s", errorCode, message));
    }
}
