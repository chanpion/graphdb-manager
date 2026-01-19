package com.graphdb.api.exception;

import com.graphdb.api.constant.ResponseCode;
import lombok.Getter;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final ResponseCode code;
    
    public BusinessException(ResponseCode code) {
        super(code.getMessage());
        this.code = code;
    }
    
    public BusinessException(ResponseCode code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(ResponseCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public BusinessException(ResponseCode code, Throwable cause) {
        super(code.getMessage(), cause);
        this.code = code;
    }
}