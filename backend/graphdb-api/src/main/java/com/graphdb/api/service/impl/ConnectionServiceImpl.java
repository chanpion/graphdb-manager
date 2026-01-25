package com.graphdb.api.service.impl;

import com.graphdb.api.service.ConnectionService;
import com.graphdb.api.dto.ConnectionConfigDTO;
import com.graphdb.storage.entity.ConnectionEntity;
import com.graphdb.storage.repository.ConnectionMapper;
import com.graphdb.service.GraphService;
import com.graphdb.core.constant.DatabaseTypeEnum;
import com.graphdb.core.model.ConnectionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Base64;
import java.time.LocalDateTime;

/**
 * 连接管理服务实现
 */
@Service
public class ConnectionServiceImpl implements ConnectionService {
    
    @Autowired
    private ConnectionMapper connectionMapper;
    
    @Autowired
    private GraphService graphService;
    
    @Override
    public List<ConnectionConfigDTO> list() {
        List<ConnectionEntity> entities = connectionMapper.selectList(null);
        return entities.stream()
                .filter(entity -> entity.getDeleted() == 0)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ConnectionConfigDTO getById(Long id) {
        ConnectionEntity entity = connectionMapper.selectById(id);
        if (entity == null || entity.getDeleted() == 1) {
            throw new RuntimeException("连接不存在");
        }
        return convertToDTO(entity);
    }
    
    @Override
    public ConnectionConfigDTO getDetail(Long id) {
        return getById(id);
    }
    
    @Override
    public Boolean testConnection(String databaseType, String host, Integer port, String username, String password, String databaseName) {
        try {
            // 创建临时配置进行连接测试
            ConnectionConfig config = new ConnectionConfig();
            config.setType(databaseType);
            config.setHost(host);
            config.setPort(port);
            config.setUsername(username);
            config.setPassword(password);

            DatabaseTypeEnum dbType = DatabaseTypeEnum.fromCode(databaseType);
            return graphService.getAdapter(dbType).testConnection(config);
        } catch (Exception e) {
            throw new RuntimeException("连接测试失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ConnectionConfigDTO create(ConnectionConfigDTO configDTO) {
        // 检查名称是否重复
        ConnectionEntity existing = connectionMapper.selectByName(configDTO.getName());
        if (existing != null && existing.getDeleted() == 0) {
            throw new RuntimeException("连接名称已存在");
        }
        
        ConnectionEntity entity = convertToEntity(configDTO);
        entity.setStatus(0); // 未测试
        entity.setCreatedBy("admin");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(0);
        
        // 直接存储密码（不再加密）
        if (configDTO.getPassword() != null) {
            entity.setPassword(configDTO.getPassword());
        }
        
        connectionMapper.insert(entity);
        
        // 设置ID返回
        configDTO.setId(entity.getId());
        configDTO.setStatus(entity.getStatus());
        configDTO.setCreatedBy(entity.getCreatedBy());
        configDTO.setCreatedAt(entity.getCreatedAt());
        configDTO.setUpdatedAt(entity.getUpdatedAt());
        return configDTO;
    }
    
    @Override
    @Transactional
    public ConnectionConfigDTO update(Long id, ConnectionConfigDTO configDTO) {
        ConnectionEntity entity = connectionMapper.selectById(id);
        if (entity == null || entity.getDeleted() == 1) {
            throw new RuntimeException("连接不存在");
        }
        
        // 如果修改了名称，检查重复
        if (!entity.getName().equals(configDTO.getName())) {
            ConnectionEntity existing = connectionMapper.selectByName(configDTO.getName());
            if (existing != null && existing.getDeleted() == 0 && !existing.getId().equals(id)) {
                throw new RuntimeException("连接名称已存在");
            }
        }
        
        // 更新字段
        entity.setName(configDTO.getName());
        entity.setType(configDTO.getType());
        entity.setHost(configDTO.getHost());
        entity.setPort(configDTO.getPort());
        entity.setUsername(configDTO.getUsername());
        // 映射DTO字段到Entity字段（注意字段名不一致）
        entity.setStorageType(configDTO.getStorageBackend());
        entity.setStorageConfig(configDTO.getStorageConfig());
        entity.setExtraParams(configDTO.getJsonParams());
        entity.setDescription(configDTO.getDescription());

        entity.setUpdatedAt(LocalDateTime.now());
        
        // 如果提供了新密码，则更新密码
        if (configDTO.getPassword() != null && !configDTO.getPassword().isEmpty()) {
            entity.setPassword(configDTO.getPassword());
        }
        
        connectionMapper.updateById(entity);
        
        return convertToDTO(entity);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        ConnectionEntity entity = connectionMapper.selectById(id);
        if (entity == null || entity.getDeleted() == 1) {
            throw new RuntimeException("连接不存在");
        }
        
        // 逻辑删除
        entity.setDeleted(1);
        entity.setUpdatedAt(LocalDateTime.now());
        connectionMapper.updateById(entity);
    }
    
    @Override
    public Boolean testConnection(Long id) {
        ConnectionConfigDTO configDTO = getById(id);
        ConnectionConfig config = convertDTOToModel(configDTO);
        
        // 直接获取密码
        String password = connectionMapper.selectById(id).getPassword();
        if (password != null) {
            config.setPassword(password);
        }
        
        // 通过GraphService调用适配器进行测试
        DatabaseTypeEnum dbType = DatabaseTypeEnum.fromCode(config.getType());
        try {
            boolean success = graphService.getAdapter(dbType).testConnection(config);
            // 更新连接状态
            ConnectionEntity entity = connectionMapper.selectById(id);
            entity.setStatus(success ? 1 : 2);
            entity.setUpdatedAt(LocalDateTime.now());
            connectionMapper.updateById(entity);
            return success;
        } catch (Exception e) {
            // 更新为异常状态
            ConnectionEntity entity = connectionMapper.selectById(id);
            entity.setStatus(2);
            entity.setUpdatedAt(LocalDateTime.now());
            connectionMapper.updateById(entity);
            throw new RuntimeException("连接测试失败: " + e.getMessage());
        }
    }
    
    @Override
    public Boolean testConnectionWithDatabase(Long id, String databaseType, String graphName) {
        // 如果不指定databaseType，使用连接配置中的类型
        if (databaseType == null || databaseType.isEmpty()) {
            return testConnection(id);
        }
        
        // 创建临时配置用于测试指定数据库
        ConnectionConfigDTO configDTO = getById(id);
        ConnectionConfig config = convertDTOToModel(configDTO);
        config.setType(databaseType);
        
        // 直接获取密码
        String password = connectionMapper.selectById(id).getPassword();
        if (password != null) {
            config.setPassword(password);
        }
        
        DatabaseTypeEnum dbType = DatabaseTypeEnum.fromCode(databaseType);
        try {
            boolean success = graphService.getAdapter(dbType).testConnection(config);
            // 更新连接状态（使用原连接ID）
            ConnectionEntity entity = connectionMapper.selectById(id);
            entity.setStatus(success ? 1 : 2);
            entity.setUpdatedAt(LocalDateTime.now());
            connectionMapper.updateById(entity);
            return success;
        } catch (Exception e) {
            ConnectionEntity entity = connectionMapper.selectById(id);
            entity.setStatus(2);
            entity.setUpdatedAt(LocalDateTime.now());
            connectionMapper.updateById(entity);
            throw new RuntimeException("连接测试失败: " + e.getMessage());
        }
    }
    
    // 转换方法
    private ConnectionConfigDTO convertToDTO(ConnectionEntity entity) {
        ConnectionConfigDTO dto = new ConnectionConfigDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setHost(entity.getHost());
        dto.setPort(entity.getPort());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        dto.setStorageBackend(entity.getStorageType());
        dto.setStorageConfig(entity.getStorageConfig());
        dto.setJsonParams(entity.getExtraParams());
        dto.setStorageType(entity.getStorageType());
        dto.setExtraParams(entity.getExtraParams());
        dto.setStatus(entity.getStatus());
        dto.setDescription(entity.getDescription());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    private ConnectionEntity convertToEntity(ConnectionConfigDTO dto) {
        ConnectionEntity entity = new ConnectionEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setHost(dto.getHost());
        entity.setPort(dto.getPort());
        entity.setUsername(dto.getUsername());
        // 直接存储密码
        // 映射DTO字段到Entity字段
        entity.setStorageType(dto.getStorageBackend());
        entity.setStorageConfig(dto.getStorageConfig());
        entity.setExtraParams(dto.getJsonParams());
        entity.setStatus(dto.getStatus());
        entity.setDescription(dto.getDescription());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }
    
    private ConnectionConfig convertDTOToModel(ConnectionConfigDTO dto) {
        ConnectionConfig config = new ConnectionConfig();
        config.setId(dto.getId());
        config.setName(dto.getName());
        config.setType(dto.getType());
        config.setHost(dto.getHost());
        config.setPort(dto.getPort());
        config.setUsername(dto.getUsername());
        config.setPassword(dto.getPassword());
        config.setStorageBackend(dto.getStorageBackend());
        config.setStorageHost(dto.getStorageHost());
        config.setJsonParams(dto.getJsonParams());
        config.setStorageType(dto.getStorageType());
        config.setStorageConfig(dto.getStorageConfig());
        config.setExtraParams(dto.getExtraParams());
        config.setStatus(dto.getStatus());
        config.setDescription(dto.getDescription());

        config.setCreatedBy(dto.getCreatedBy());
        return config;
    }
}
