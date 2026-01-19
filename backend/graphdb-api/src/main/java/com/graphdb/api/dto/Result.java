package com.graphdb.api.dto;

import com.graphdb.api.constant.ResponseCode;
import lombok.Data;
import java.util.UUID;

/**
 * 统一响应结果
 * 按照API设计文档要求，包含timestamp和traceId字段
 */
@Data
public class Result<T> {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 请求追踪ID
     */
    private String traceId;
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResponseCode.SUCCESS.getCode());
        result.setMessage("操作成功");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(generateTraceId());
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResponseCode.INTERNAL_SERVER_ERROR.getCode());
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(generateTraceId());
        return result;
    }
    
    public static <T> Result<T> error(ResponseCode code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code.getCode());
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(generateTraceId());
        return result;
    }
    
    /**
     * 分页响应
     */
    public static <T> Result<PageResult<T>> success(T data, long total, int pageNum, int pageSize) {
        PageResult<T> pageResult = new PageResult<>(data, total, pageNum, pageSize);
        return Result.success(pageResult);
    }
    
    /**
     * 生成追踪ID
     */
    private static String generateTraceId() {
        return "req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 分页结果包装类
     */
    @Data
    public static class PageResult<T> {
        private long total;
        private int pageNum;
        private int pageSize;
        private T list;
        
        public PageResult(T list, long total, int pageNum, int pageSize) {
            this.list = list;
            this.total = total;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
        }
    }
}
