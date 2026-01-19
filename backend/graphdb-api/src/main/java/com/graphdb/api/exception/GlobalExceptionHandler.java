package com.graphdb.api.exception;

import com.graphdb.api.dto.Result;
import com.graphdb.api.constant.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 按照API设计文档要求统一处理各种异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e, WebRequest request) {
        log.warn("业务异常: {}", e.getMessage(), e);
        
        Result<?> response = Result.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(getHttpStatus(e.getCode())).body(response);
    }
    
    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("参数校验异常: {}", e.getMessage());
        
        BindingResult bindingResult = e.getBindingResult();
        String errorMessage = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        Result<?> response = Result.error(ResponseCode.BAD_REQUEST, "参数验证失败: " + errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 参数校验异常处理（方法级别）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<?>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("参数校验异常: {}", e.getMessage());
        
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        Result<?> response = Result.error(ResponseCode.BAD_REQUEST, "参数验证失败: " + errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 404异常处理
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<?>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        
        Result<?> response = Result.error(ResponseCode.NOT_FOUND, "请求的资源不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * 其他运行时异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<?>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        
        Result<?> response = Result.error(ResponseCode.INTERNAL_SERVER_ERROR, "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 其他异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        
        Result<?> response = Result.error(ResponseCode.INTERNAL_SERVER_ERROR, "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 根据错误码获取HTTP状态码
     */
    private HttpStatus getHttpStatus(ResponseCode code) {
        int codeInt = code.getCode();
        if (codeInt >= 40000 && codeInt < 50000) {
            return getBusinessErrorHttpStatus(codeInt);
        } else {
            return getStandardHttpStatus(codeInt);
        }
    }
    
    /**
     * 业务错误码映射到HTTP状态码
     */
    private HttpStatus getBusinessErrorHttpStatus(int code) {
        // 40000系列：客户端错误
        if (code >= 40000 && code < 41000) {
            return HttpStatus.BAD_REQUEST;
        }
        // 41000系列：资源未找到
        if (code >= 41000 && code < 42000) {
            return HttpStatus.NOT_FOUND;
        }
        // 42000系列：数据操作错误
        if (code >= 42000 && code < 43000) {
            return HttpStatus.BAD_REQUEST;
        }
        // 43000系列：Schema错误
        if (code >= 43000 && code < 44000) {
            return HttpStatus.BAD_REQUEST;
        }
        // 44000系列：导入导出错误
        if (code >= 44000 && code < 45000) {
            return HttpStatus.BAD_REQUEST;
        }
        // 45000系列：查询错误
        if (code >= 45000 && code < 46000) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    /**
     * 标准HTTP状态码映射
     */
    private HttpStatus getStandardHttpStatus(int code) {
        try {
            return HttpStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}