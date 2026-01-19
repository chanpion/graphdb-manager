package com.graphdb.api.constant;

/**
 * 统一响应码枚举
 * 按照API设计文档定义完整的错误码体系
 */
public enum ResponseCode {
    // 成功响应
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    
    // 通用错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 连接管理错误码 (40000系列)
    CONNECTION_NAME_EXISTS(40001, "连接名称已存在"),
    CONNECTION_PARAM_INVALID(40002, "连接参数无效"),
    CONNECTION_TEST_FAILED(40003, "连接测试失败"),
    CONNECTION_NOT_FOUND(40004, "连接不存在"),
    CONNECTION_DISABLED(40005, "连接已禁用"),
    
    // 图管理错误码 (41000系列)
    GRAPH_NOT_FOUND(41001, "图不存在"),
    GRAPH_NAME_EXISTS(41002, "图名称已存在"),
    GRAPH_CREATE_FAILED(41003, "创建图失败"),
    GRAPH_DELETE_FAILED(41004, "删除图失败"),
    GRAPH_UNAVAILABLE(41005, "图不可用"),
    
    // 数据操作错误码 (42000系列)
    VERTEX_NOT_FOUND(42001, "节点UID不存在"),
    EDGE_NOT_FOUND(42002, "边UID不存在"),
    UID_DUPLICATE(42003, "UID重复"),
    NODE_TYPE_NOT_FOUND(42004, "节点类型不存在"),
    EDGE_TYPE_NOT_FOUND(42005, "边类型不存在"),
    REFERENCE_VIOLATION(42006, "引用完整性违规"),
    DATA_VALIDATION_FAILED(42007, "数据验证失败"),
    BATCH_OPERATION_LIMIT_EXCEEDED(42008, "批量操作超出限制"),
    
    // Schema管理错误码 (43000系列)
    NODE_TYPE_EXISTS(43001, "节点类型已存在"),
    EDGE_TYPE_EXISTS(43002, "边类型已存在"),
    PROPERTY_DEFINITION_INVALID(43003, "属性定义无效"),
    INDEX_CREATE_FAILED(43004, "索引创建失败"),
    
    // 导入导出错误码 (44000系列)
    FILE_FORMAT_NOT_SUPPORTED(44001, "文件格式不支持"),
    FILE_PARSE_FAILED(44002, "文件解析失败"),
    FIELD_MAPPING_ERROR(44003, "字段映射错误"),
    IMPORT_PARTIAL_FAILURE(44004, "导入部分失败"),
    
    // 原生查询错误码 (45000系列)
    QUERY_EMPTY(45001, "查询语句为空"),
    QUERY_SYNTAX_ERROR(45002, "查询语法错误"),
    QUERY_TIMEOUT(45003, "查询超时"),
    QUERY_RESULT_LIMIT_EXCEEDED(45004, "查询结果超限"),
    QUERY_COMPLEXITY_EXCEEDED(45005, "查询复杂度超限"),
    QUERY_LANGUAGE_NOT_SUPPORTED(45006, "图数据库不支持该查询语法"),
    QUERY_EXECUTION_FAILED(45007, "查询执行失败"),
    QUERY_INJECTION_RISK(45008, "查询注入风险");
    
    private final int code;
    private final String message;
    
    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
