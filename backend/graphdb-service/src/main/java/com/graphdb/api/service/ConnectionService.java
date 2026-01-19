package com.graphdb.api.service;

import com.graphdb.api.dto.ConnectionConfigDTO;
import java.util.List;

/**
 * 连接管理服务接口
 */
public interface ConnectionService {
    
    /**
     * 获取所有连接
     */
    List<ConnectionConfigDTO> list();
    
    /**
     * 根据ID获取连接
     */
    ConnectionConfigDTO getById(Long id);
    
    /**
     * 获取连接详情（同getById，为了Controller兼容性）
     */
    ConnectionConfigDTO getDetail(Long id);
    
    /**
     * 独立测试连接（无需连接ID）
     */
    Boolean testConnection(String databaseType, String host, Integer port, String username, String password, String databaseName);
    
    /**
     * 创建连接
     */
    ConnectionConfigDTO create(ConnectionConfigDTO config);
    
    /**
     * 更新连接
     */
    ConnectionConfigDTO update(Long id, ConnectionConfigDTO config);
    
    /**
     * 删除连接
     */
    void delete(Long id);
    
    /**
     * 测试连接
     */
    Boolean testConnection(Long id);
    
    /**
     * 测试连接（指定数据库类型和图名）
     */
    Boolean testConnectionWithDatabase(Long id, String databaseType, String graphName);
}
