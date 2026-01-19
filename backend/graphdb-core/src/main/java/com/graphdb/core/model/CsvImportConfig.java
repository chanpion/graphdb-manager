package com.graphdb.core.model;

import lombok.Data;

/**
 * CSV导入配置
 */
@Data
public class CsvImportConfig {
    
    /**
     * CSV分隔符，默认逗号
     */
    private String delimiter = ",";
    
    /**
     * 是否包含表头，默认true
     */
    private boolean hasHeader = true;
    
    /**
     * 引用字符，默认双引号
     */
    private String quoteChar = "\"";
    
    /**
     * 编码格式，默认UTF-8
     */
    private String encoding = "UTF-8";
    
    /**
     * 节点标签字段名
     */
    private String labelField;
    
    /**
     * 节点ID字段名
     */
    private String idField;
    
    /**
     * 边源节点字段名
     */
    private String sourceField;
    
    /**
     * 边目标节点字段名
     */
    private String targetField;
    
    /**
     * 是否忽略解析错误，默认false
     */
    private boolean ignoreParseErrors = false;
    
    /**
     * 导入模式
     */
    private ImportMode importMode;
    
    /**
     * 批处理大小
     */
    private int batchSize = 1000;
    
    /**
     * 字段映射配置
     * key: CSV字段名, value: 目标字段类型
     */
    private Map<String, String> fieldMapping;
}