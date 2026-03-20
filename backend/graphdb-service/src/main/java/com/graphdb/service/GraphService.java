package com.graphdb.service;

import com.graphdb.core.interfaces.GraphAdapter;
import com.graphdb.core.interfaces.DataHandler;
import com.graphdb.core.constant.DatabaseTypeEnum;
import com.graphdb.core.constant.GraphSourceEnum;
import com.graphdb.core.model.ConnectionConfig;
import com.graphdb.core.model.GraphSchema;
import com.graphdb.core.model.LabelType;
import com.graphdb.core.model.Vertex;
import com.graphdb.core.model.Edge;
import com.graphdb.core.model.QueryCondition;
import com.graphdb.core.model.CsvImportConfig;
import com.graphdb.core.exception.CoreException;
import com.graphdb.api.service.ConnectionService;
import com.graphdb.api.dto.ConnectionConfigDTO;
import com.graphdb.storage.repository.GraphInstanceMapper;
import com.graphdb.storage.entity.GraphInstanceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图服务
 * 负责图管理的业务逻辑
 */
@Service
public class GraphService {
    
    private final Map<DatabaseTypeEnum, GraphAdapter> adapterMap = new ConcurrentHashMap<>();
    
    @Autowired
    private ConnectionService connectionService;
    
    @Autowired
    private GraphInstanceMapper graphInstanceMapper;
    
    /**
     * 自动注入所有适配器
     */
    @Autowired
    public GraphService(List<GraphAdapter> adapters) {
        if (adapters != null) {
            for (GraphAdapter adapter : adapters) {
                adapterMap.put(adapter.getDatabaseType(), adapter);
                System.out.println("Registered adapter for: " + adapter.getDatabaseType());
            }
        }
    }
    
    /**
     * 根据数据库类型获取适配器
     * @param databaseType 数据库类型
     * @return 适配器实例
     */
    public GraphAdapter getAdapter(DatabaseTypeEnum databaseType) {
        return adapterMap.get(databaseType);
    }
    
    /**
     * 测试适配器连接
     * @param databaseType 数据库类型
     * @param config 连接配置
     * @return 是否连接成功
     */
    public boolean testAdapter(DatabaseTypeEnum databaseType, ConnectionConfig config) {
        GraphAdapter adapter = getAdapter(databaseType);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + databaseType);
        }
        try {
            return adapter.testConnection(config);
        } catch (CoreException e) {
            throw new RuntimeException("适配器测试连接异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有图实例
     * @param connectionId 连接ID
     * @return 图实例列表
     */
    public List<Map<String, Object>> getAllGraphs(Long connectionId) {
        // TODO: 从各适配器获取图列表
        return List.of();
    }
    
    
    public void registerAdapter(GraphAdapter adapter) {
        adapterMap.put(adapter.getDatabaseType(), adapter);
    }
    
    
    public GraphAdapter getAdapter(String databaseType) {
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(databaseType);
        return adapterMap.get(type);
    }
    
    
    public List<String> getGraphs(Long connectionId) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            return adapter.getGraphs(config);
        } catch (CoreException e) {
            throw new RuntimeException("获取图列表失败: " + e.getMessage(), e);
        }
    }
    
    
    public GraphSchema getGraphSchema(Long connectionId, String graphName) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            return adapter.getGraphSchema(config, graphName);
        } catch (CoreException e) {
            throw new RuntimeException("获取图schema失败: " + e.getMessage(), e);
        }
    }
    
    
    public void createGraph(Long connectionId, String graphName) {
        createGraph(connectionId, graphName, null);
    }
        
    /**
     * 创建图
     * @param connectionId 连接 ID
     * @param graphName 图名称（标识）
     * @param description 图描述（可选）
     */
    public void createGraph(Long connectionId, String graphName, String description) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器：" + type);
        }
        try {
            // 在数据库中创建图空间
            adapter.createGraph(config, graphName);
                
            // 同时在本地数据库记录图实例信息
            try {
                GraphInstanceEntity entity = new GraphInstanceEntity();
                entity.setConnectionId(connectionId);
                entity.setGraphName(graphName);
                entity.setDatabaseType(config.getType());
                entity.setSourceType(GraphSourceEnum.PLATFORM.getCode());
                entity.setStatus("NORMAL");
                entity.setDescription(description != null ? description : "平台创建的图");
                entity.setVertexCount(0L);
                entity.setEdgeCount(0L);
                graphInstanceMapper.insert(entity);
            } catch (Exception e) {
                System.out.println("警告：保存图实例信息到本地数据库失败：" + e.getMessage());
                // 不抛出异常，不影响图的创建
            }
        } catch (CoreException e) {
            throw new RuntimeException("创建图失败：" + e.getMessage(), e);
        }
    }
    
    
    public void deleteGraph(Long connectionId, String graphName) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            adapter.deleteGraph(config, graphName);
        } catch (CoreException e) {
            throw new RuntimeException("删除图失败: " + e.getMessage(), e);
        }
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
    
    
    public List<LabelType> getVertexTypes(Long connectionId, String graphName) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            return adapter.getVertexTypes(config, graphName);
        } catch (CoreException e) {
            throw new RuntimeException("获取点类型列表失败: " + e.getMessage(), e);
        }
    }
    
    
    public List<LabelType> getEdgeTypes(Long connectionId, String graphName) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            return adapter.getEdgeTypes(config, graphName);
        } catch (CoreException e) {
            throw new RuntimeException("获取边类型列表失败: " + e.getMessage(), e);
        }
    }
    
    
    public void createVertexType(Long connectionId, String graphName, LabelType labelType) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            adapter.createVertexType(config, graphName, labelType);
        } catch (CoreException e) {
            throw new RuntimeException("创建点类型失败: " + e.getMessage(), e);
        }
    }
    
    
    public void deleteVertexType(Long connectionId, String graphName, String labelName) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            adapter.deleteVertexType(config, graphName, labelName);
        } catch (CoreException e) {
            throw new RuntimeException("删除点类型失败: " + e.getMessage(), e);
        }
    }
    
    
    public void createEdgeType(Long connectionId, String graphName, LabelType labelType) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            adapter.createEdgeType(config, graphName, labelType);
        } catch (CoreException e) {
            throw new RuntimeException("创建边类型失败: " + e.getMessage(), e);
        }
    }
    
    
    public void deleteEdgeType(Long connectionId, String graphName, String labelName) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            adapter.deleteEdgeType(config, graphName, labelName);
        } catch (CoreException e) {
            throw new RuntimeException("删除边类型失败: " + e.getMessage(), e);
        }
    }
    
    // ========== 数据操作方法实现 ==========
    
    
    public Vertex createVertex(Long connectionId, String graphName, String label, Map<String, Object> properties) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            // 检查适配器是否实现了DataHandler接口
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            Map<String, Object> vertexMap = dataHandler.createVertex(graphName, label, properties);
            return convertMapToVertex(vertexMap);
        } catch (CoreException e) {
            throw new RuntimeException("创建节点失败: " + e.getMessage(), e);
        }
    }
    
    
    public List<Vertex> queryVertices(Long connectionId, String graphName, QueryCondition condition) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            // 目前只支持标签过滤，后续可以扩展属性过滤
            String label = condition != null ? condition.getLabel() : null;
            List<Map<String, Object>> vertexMaps = dataHandler.queryVertices(graphName, label);
            return vertexMaps.stream()
                    .map(this::convertMapToVertex)
                    .toList();
        } catch (CoreException e) {
            throw new RuntimeException("查询节点失败: " + e.getMessage(), e);
        }
    }
    
    
    public Vertex updateVertex(Long connectionId, String graphName, String uid, Map<String, Object> properties) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            Map<String, Object> vertexMap = dataHandler.updateVertex(graphName, uid, properties);
            return convertMapToVertex(vertexMap);
        } catch (CoreException e) {
            throw new RuntimeException("更新节点失败: " + e.getMessage(), e);
        }
    }
    
    
    public void deleteVertex(Long connectionId, String graphName, String uid) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            dataHandler.deleteVertex(graphName, uid);
        } catch (CoreException e) {
            throw new RuntimeException("删除节点失败: " + e.getMessage(), e);
        }
    }
    
    
    public Edge createEdge(Long connectionId, String graphName, String label, 
                          String sourceUid, String targetUid, Map<String, Object> properties) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            Map<String, Object> edgeMap = dataHandler.createEdge(graphName, label, sourceUid, targetUid, properties);
            return convertMapToEdge(edgeMap);
        } catch (CoreException e) {
            throw new RuntimeException("创建边失败: " + e.getMessage(), e);
        }
    }
    
    
    public List<Edge> queryEdges(Long connectionId, String graphName, QueryCondition condition) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            String label = condition != null ? condition.getLabel() : null;
            List<Map<String, Object>> edgeMaps = dataHandler.queryEdges(graphName, label);
            return edgeMaps.stream()
                    .map(this::convertMapToEdge)
                    .toList();
        } catch (CoreException e) {
            throw new RuntimeException("查询边失败: " + e.getMessage(), e);
        }
    }
    
    
    public Edge updateEdge(Long connectionId, String graphName, String uid, Map<String, Object> properties) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            Map<String, Object> edgeMap = dataHandler.updateEdge(graphName, uid, properties);
            return convertMapToEdge(edgeMap);
        } catch (CoreException e) {
            throw new RuntimeException("更新边失败: " + e.getMessage(), e);
        }
    }
    
    
    public void deleteEdge(Long connectionId, String graphName, String uid) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            dataHandler.deleteEdge(graphName, uid);
        } catch (CoreException e) {
            throw new RuntimeException("删除边失败: " + e.getMessage(), e);
        }
    }
    
    
    public Object executeNativeQuery(Long connectionId, String graphName, 
                                    String queryLanguage, String queryStatement) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            return adapter.executeNativeQuery(config, graphName, queryLanguage, queryStatement);
        } catch (CoreException e) {
            throw new RuntimeException("执行原生查询失败: " + e.getMessage(), e);
        }
    }
    
    
    public String importFromCsv(Long connectionId, String graphName, 
                               CsvImportConfig config, java.io.InputStream csvStream) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig configModel = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(configModel.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            return dataHandler.importFromCsv(graphName, config, csvStream);
        } catch (CoreException e) {
            throw new RuntimeException("CSV导入失败: " + e.getMessage(), e);
        }
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 将Map转换为Vertex对象
     */
    private Vertex convertMapToVertex(Map<String, Object> vertexMap) {
        if (vertexMap == null) {
            return null;
        }
        Vertex vertex = new Vertex();
        vertex.setUid((String) vertexMap.get("uid"));
        vertex.setLabel((String) vertexMap.get("label"));
        
        // 处理properties字段
        Object propertiesObj = vertexMap.get("properties");
        if (propertiesObj instanceof Map) {
            vertex.setProperties(new HashMap<>((Map<String, Object>) propertiesObj));
        }
        
        // 处理时间戳字段
        Object createdAtObj = vertexMap.get("createdAt");
        if (createdAtObj instanceof Number) {
            vertex.setCreatedAt(((Number) createdAtObj).longValue());
        }
        
        Object updatedAtObj = vertexMap.get("updatedAt");
        if (updatedAtObj instanceof Number) {
            vertex.setUpdatedAt(((Number) updatedAtObj).longValue());
        }
        
        return vertex;
    }
    
    /**
     * 将Map转换为Edge对象
     */
    private Edge convertMapToEdge(Map<String, Object> edgeMap) {
        if (edgeMap == null) {
            return null;
        }
        Edge edge = new Edge();
        edge.setUid((String) edgeMap.get("uid"));
        edge.setLabel((String) edgeMap.get("label"));
        edge.setSourceUid((String) edgeMap.get("sourceUid"));
        edge.setTargetUid((String) edgeMap.get("targetUid"));
        
        // 处理properties字段
        Object propertiesObj = edgeMap.get("properties");
        if (propertiesObj instanceof Map) {
            edge.setProperties(new HashMap<>((Map<String, Object>) propertiesObj));
        }
        
        // 处理时间戳字段
        Object createdAtObj = edgeMap.get("createdAt");
        if (createdAtObj instanceof Number) {
            edge.setCreatedAt(((Number) createdAtObj).longValue());
        }
        
        Object updatedAtObj = edgeMap.get("updatedAt");
        if (updatedAtObj instanceof Number) {
            edge.setUpdatedAt(((Number) updatedAtObj).longValue());
        }
        
        return edge;
    }
    
    /**
     * 获取图详情
     */
    public Map<String, Object> getGraphDetail(Long connectionId, String graphName) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            // 目前返回基本图信息，后续可以扩展为包含更多详细信息
            Map<String, Object> detail = new HashMap<>();
            detail.put("name", graphName);
            detail.put("connectionId", connectionId);
            detail.put("databaseType", config.getType());
            detail.put("vertexCount", 0); // 需要从适配器获取实际数据
            detail.put("edgeCount", 0);    // 需要从适配器获取实际数据
            detail.put("schemaInfo", adapter.getGraphSchema(config, graphName));
            return detail;
        } catch (CoreException e) {
            throw new RuntimeException("获取图详情失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量删除边
     */
    public void batchDeleteEdges(Long connectionId, String graphName, List<String> uids) {
        ConnectionConfigDTO dto = connectionService.getById(connectionId);
        ConnectionConfig config = convertDTOToModel(dto);
        DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
        GraphAdapter adapter = getAdapter(type);
        if (adapter == null) {
            throw new RuntimeException("未找到对应数据库类型的适配器: " + type);
        }
        try {
            if (!(adapter instanceof DataHandler)) {
                throw new RuntimeException("适配器 " + type + " 未实现DataHandler接口");
            }
            DataHandler dataHandler = (DataHandler) adapter;
            
            // 批量删除边
            for (String uid : uids) {
                dataHandler.deleteEdge(graphName, uid);
            }
        } catch (CoreException e) {
            throw new RuntimeException("批量删除边失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取图实例列表，支持按来源筛选
     * @param connectionId 连接 ID
     * @param sourceType 图来源类型（PLATFORM/EXISTING），可选
     * @return 图实例列表
     */
    public List<GraphInstanceEntity> getGraphInstances(Long connectionId, String sourceType) {
        if (sourceType != null && !sourceType.isEmpty()) {
            // 验证 sourceType 是否有效
            if (!GraphSourceEnum.isValid(sourceType)) {
                throw new IllegalArgumentException("无效的图来源类型：" + sourceType);
            }
                
            // 如果是查询已存在的图，需要从数据库适配器中获取
            if (GraphSourceEnum.EXISTING.getCode().equals(sourceType)) {
                return getExistingGraphsFromDatabase(connectionId);
            } else {
                // 查询平台创建的图
                return graphInstanceMapper.selectByConnectionIdAndSourceType(connectionId, sourceType);
            }
        } else {
            // 不指定 sourceType 时，同时获取已存在和平台创建的图
            List<GraphInstanceEntity> platformGraphs = graphInstanceMapper.selectByConnectionIdAndSourceType(
                connectionId, GraphSourceEnum.PLATFORM.getCode());
            List<GraphInstanceEntity> existingGraphs = getExistingGraphsFromDatabase(connectionId);
                
            // 合并两个列表
            java.util.stream.Stream<GraphInstanceEntity> combined = java.util.stream.Stream.concat(
                platformGraphs.stream(), 
                existingGraphs.stream()
            );
            return combined.toList();
        }
    }
        
    /**
     * 从数据库适配器获取已存在的图列表
     * @param connectionId 连接 ID
     * @return 已存在的图实例列表
     */
    private List<GraphInstanceEntity> getExistingGraphsFromDatabase(Long connectionId) {
        try {
            // 获取连接配置
            ConnectionConfigDTO dto = connectionService.getById(connectionId);
            ConnectionConfig config = convertDTOToModel(dto);
            DatabaseTypeEnum type = DatabaseTypeEnum.fromCode(config.getType());
            GraphAdapter adapter = getAdapter(type);
                
            if (adapter == null) {
                throw new RuntimeException("未找到对应数据库类型的适配器：" + type);
            }
                
            // 调用适配器的 getGraphs 方法获取已存在的图（如 Nebula 的 SHOW SPACES）
            List<String> graphNames = adapter.getGraphs(config);
                
            // 将图名称转换为 GraphInstanceEntity 列表，并统计每个图的点边数量
            return graphNames.stream().map(graphName -> {
                GraphInstanceEntity entity = new GraphInstanceEntity();
                entity.setConnectionId(connectionId);
                entity.setGraphName(graphName);
                entity.setSourceType(GraphSourceEnum.EXISTING.getCode());
                entity.setStatus("ACTIVE");
                entity.setDescription("图数据库已有的图空间");
                        
                // 统计该图的点边数量
                try {
                    // 检查适配器是否实现了 DataHandler 接口
                    if (adapter instanceof DataHandler) {
                        DataHandler dataHandler = (DataHandler) adapter;
                        // 直接调用统计方法获取实际的点边数量
                        Long vertexCount = dataHandler.countVertices(graphName, null);
                        Long edgeCount = dataHandler.countEdges(graphName, null);
                        entity.setVertexCount(vertexCount);
                        entity.setEdgeCount(edgeCount);
                    } else {
                        // 如果没有实现 DataHandler，尝试从 schema 获取
                        GraphSchema schema = adapter.getGraphSchema(config, graphName);
                        if (schema != null) {
                            entity.setVertexCount(calculateVertexCount(schema));
                            entity.setEdgeCount(calculateEdgeCount(schema));
                        } else {
                            entity.setVertexCount(0L);
                            entity.setEdgeCount(0L);
                        }
                    }
                } catch (Exception e) {
                    // 如果获取失败，设置为 0
                    entity.setVertexCount(0L);
                    entity.setEdgeCount(0L);
                }
                        
                return entity;
            }).toList();
                    
        } catch (Exception e) {
            throw new RuntimeException("获取已存在的图列表失败：" + e.getMessage(), e);
        }
    }
        
    /**
     * 计算点总数
     * @param schema 图 schema
     * @return 点总数
     */
    private Long calculateVertexCount(GraphSchema schema) {
        if (schema == null || schema.getVertexLabels() == null) {
            return 0L;
        }
            
        // 尝试从 statistics 中获取
        if (schema.getStatistics() != null && schema.getStatistics().containsKey("vertexCount")) {
            Object count = schema.getStatistics().get("vertexCount");
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        }
            
        // TODO: 需要从 DataHandler 获取实际的点数量统计
        // 目前返回 0，后续可以通过执行 COUNT 查询来获取实际数量
        return 0L;
    }
        
    /**
     * 计算边总数
     * @param schema 图 schema
     * @return 边总数
     */
    private Long calculateEdgeCount(GraphSchema schema) {
        if (schema == null || schema.getEdgeLabels() == null) {
            return 0L;
        }
            
        // 尝试从 statistics 中获取
        if (schema.getStatistics() != null && schema.getStatistics().containsKey("edgeCount")) {
            Object count = schema.getStatistics().get("edgeCount");
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        }
            
        // TODO: 需要从 DataHandler 获取实际的边数量统计
        // 目前返回 0，后续可以通过执行 COUNT 查询来获取实际数量
        return 0L;
    }
    
    /**
     * 获取图名称列表，支持按来源筛选
     * @param connectionId 连接ID
     * @param sourceType 图来源类型（PLATFORM/EXISTING），可选
     * @return 图名称列表
     */
    public List<String> getGraphs(Long connectionId, String sourceType) {
        List<GraphInstanceEntity> instances = getGraphInstances(connectionId, sourceType);
        return instances.stream()
                .map(GraphInstanceEntity::getGraphName)
                .toList();
    }
    
    /**
     * 更新图实例信息
     * @param connectionId 连接ID
     * @param graphName 图名称
     * @param updates 更新的字段
     */
    public void updateGraph(Long connectionId, String graphName, Map<String, Object> updates) {
        // 查找现有的图实例
        GraphInstanceEntity existing = graphInstanceMapper.selectByConnectionIdAndGraphName(connectionId, graphName);
        if (existing == null) {
            throw new RuntimeException("图实例不存在: " + graphName);
        }
        
        // 更新可更新的字段
        if (updates.containsKey("description")) {
            existing.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("name")) {
            existing.setGraphName((String) updates.get("name"));
        }
        if (updates.containsKey("status")) {
            existing.setStatus(String.valueOf(updates.get("status")));
        }

        // 保存更新
        graphInstanceMapper.updateById(existing);
    }
}