package com.graphdb.service;

import java.util.Map;
import java.util.List;

/**
 * 系统管理服务接口
 */
public interface SystemService {
    
    /**
     * 获取系统健康状态
     */
    Map<String, Object> getSystemHealth();
    
    /**
     * 获取操作日志
     */
    Map<String, Object> getOperationLogs(Long connectionId, Integer page, Integer pageSize,
                                        Long startTime, Long endTime, String operationType, 
                                        String resultStatus);
    
    /**
     * 获取系统配置
     */
    Map<String, Object> getSystemConfig();
    
    /**
     * 更新系统配置
     */
    Map<String, Object> updateSystemConfig(Map<String, Object> config);
    
    /**
     * 获取性能指标
     */
    Map<String, Object> getSystemMetrics(String metricType, Long startTime, Long endTime);
    
    /**
     * 清理缓存
     */
    Map<String, Object> clearCache(String cacheType);
    
    /**
     * 备份系统数据
     */
    Map<String, Object> backupData(String backupType);
    
    /**
     * 恢复系统数据
     */
    Map<String, Object> restoreData(String backupFile, Boolean cleanRestore);
    
    /**
     * 获取系统版本信息
     */
    Map<String, Object> getVersionInfo();
    
    /**
     * 重启系统服务
     */
    Map<String, Object> restartService(String serviceName);
    
    /**
     * 获取连接池状态
     */
    Map<String, Object> getConnectionPoolStatus();
    
    /**
     * 获取数据库连接信息
     */
    Map<String, Object> getDatabaseInfo();
}