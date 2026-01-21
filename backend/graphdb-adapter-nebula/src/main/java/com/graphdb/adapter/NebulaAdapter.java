package com.graphdb.adapter;

import com.graphdb.core.interfaces.GraphAdapter;
import com.graphdb.core.interfaces.SchemaHandler;
import com.graphdb.core.interfaces.DataHandler;
import com.graphdb.core.constant.DatabaseTypeEnum;
import com.graphdb.core.model.ConnectionConfig;
import com.graphdb.core.model.GraphSchema;
import com.graphdb.core.model.LabelType;
import com.graphdb.core.model.CsvImportConfig;
import com.graphdb.core.exception.CoreException;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.InputStream;

// import static com.vesoft.nebula.client.graph.net.NebulaPool.getSession; // 移除不正确的静态导入

/**
 * NebulaGraph适配器实现
 */
@Service
public class NebulaAdapter implements GraphAdapter, SchemaHandler, DataHandler {
    
    private NebulaPool pool;
    private Session session;
    private boolean connected;
    private String currentGraph;
    
    public NebulaAdapter() {
        // 初始化连接池
    }
    
    @Override
    public DatabaseTypeEnum getDatabaseType() {
        return DatabaseTypeEnum.NEBULA;
    }
    
    @Override
    public boolean testConnection(ConnectionConfig config) {
        try {
            NebulaPoolConfig poolConfig = new NebulaPoolConfig();
            poolConfig.setMaxConnSize(1);
            
            NebulaPool testPool = new NebulaPool();
            List<HostAddress> addresses = Arrays.asList(
                new HostAddress(config.getHost(), config.getPort())
            );
            
            testPool.init(addresses, poolConfig);
            Session testSession = testPool.getSession(config.getUsername(), config.getPassword(), false);
            
            // 执行简单查询测试连接
            ResultSet result = testSession.execute("SHOW SPACES");
            testSession.release();
            testPool.close();
            
            return result.isSucceeded();
        } catch (Exception e) {
            throw new CoreException("NebulaGraph连接测试失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void connect(ConnectionConfig config) {
        try {
            NebulaPoolConfig poolConfig = new NebulaPoolConfig();
            poolConfig.setMaxConnSize(10);
            poolConfig.setMinConnSize(2);
            poolConfig.setIdleTime(1000 * 60 * 60 * 2);
            
            pool = new NebulaPool();
            List<HostAddress> addresses = Arrays.asList(
                    new HostAddress(config.getHost(), config.getPort())
            );
            
            pool.init(addresses, poolConfig);
            session = pool.getSession(config.getUsername(), config.getPassword(), false);
            
            // 设置当前图空间
            currentGraph = config.getDatabaseName() != null ? config.getDatabaseName() : "test";
            if (config.getDatabaseName() != null) {
                session.execute("USE " + config.getDatabaseName());
            }
            
            connected = true;
            System.out.println("成功连接到NebulaGraph: " + config.getHost() + ":" + config.getPort());
        } catch (Exception e) {
            throw new CoreException("NebulaGraph连接失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void disconnect() {
        if (session != null) {
            session.release();
            session = null;
        }
        if (pool != null) {
            pool.close();
            pool = null;
        }
        connected = false;
        currentGraph = null;
        System.out.println("已断开NebulaGraph连接");
    }
    
    @Override
    public boolean isConnected() {
        return connected && pool != null && session != null;
    }
    
    // DataHandler 接口实现
    @Override
    public Map<String, Object> createVertex(String graphName, String label, Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("NebulaGraph连接未建立");
        }
        
        try {
            // 生成唯一ID
            String uid = "vertex_" + System.currentTimeMillis();
            
            // 构建创建节点的nGQL查询
            StringBuilder queryBuilder = new StringBuilder("INSERT VERTEX `" + label + "` VALUES \"" + uid + "\":");
            
            if (properties != null && !properties.isEmpty()) {
                boolean first = true;
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    if (!first) queryBuilder.append(",");
                    queryBuilder.append(" ").append(formatValue(entry.getValue()));
                    first = false;
                }
            }
            queryBuilder.append(";");
            
            ResultSet result = session.execute(queryBuilder.toString());
            if (!result.isSucceeded()) {
                throw new CoreException("创建节点失败: " + result.getErrorMessage());
            }
            
            // 构建返回的节点数据
            Map<String, Object> vertex = new HashMap<>();
            vertex.put("uid", uid);
            vertex.put("label", label);
            vertex.put("properties", properties != null ? properties : new HashMap<>());
            
            return vertex;
        } catch (Exception e) {
            throw new CoreException("创建Nebula节点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> createEdge(String graphName, String label, String sourceUid, String targetUid, Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("NebulaGraph连接未建立");
        }
        
        try {
            // 生成唯一ID
            String uid = "edge_" + System.currentTimeMillis();
            
            // 构建创建边的nGQL查询
            StringBuilder queryBuilder = new StringBuilder("INSERT EDGE `" + label + "` VALUES \"" + sourceUid + "\"->\"" + targetUid + "\"");
            
            if (properties != null && !properties.isEmpty()) {
                queryBuilder.append("(");
                boolean first = true;
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    if (!first) queryBuilder.append(",");
                    queryBuilder.append(" ").append(formatValue(entry.getValue()));
                    first = false;
                }
                queryBuilder.append(")");
            }
            queryBuilder.append(";");
            
            ResultSet result = session.execute(queryBuilder.toString());
            if (!result.isSucceeded()) {
                throw new CoreException("创建边失败: " + result.getErrorMessage());
            }
            
            // 构建返回的边数据
            Map<String, Object> edge = new HashMap<>();
            edge.put("uid", uid);
            edge.put("label", label);
            edge.put("sourceUid", sourceUid);
            edge.put("targetUid", targetUid);
            edge.put("properties", properties != null ? properties : new HashMap<>());
            
            return edge;
        } catch (Exception e) {
            throw new CoreException("创建Nebula边失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteVertex(String graphName, String uid) {
        if (!isConnected()) {
            throw new CoreException("NebulaGraph连接未建立");
        }
        
        try {
            // 删除节点及其所有边
            String query = "DELETE VERTEX \"" + uid + "\";";
            ResultSet result = session.execute(query);
            if (!result.isSucceeded()) {
                throw new CoreException("删除节点失败: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            throw new CoreException("删除Nebula节点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteEdge(String graphName, String uid) {
        if (!isConnected()) {
            throw new CoreException("NebulaGraph连接未建立");
        }
        
        try {
            // NebulaGraph没有直接通过边ID删除边的API，需要通过查询来删除
            // 这里需要先查询边信息，然后删除
            // 实际实现中需要更复杂的逻辑
            System.out.println("NebulaGraph边删除需要先查询边信息，此功能需要进一步完善");
        } catch (Exception e) {
            throw new CoreException("删除Nebula边失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> queryVertices(String graphName, String label) {
        if (!isConnected()) {
            throw new CoreException("NebulaGraph连接未建立");
        }
        
        try {
            String query;
            if (label != null && !label.isEmpty()) {
                query = "MATCH (v:`" + label + "`) RETURN v LIMIT 100;";
            } else {
                query = "MATCH (v) RETURN v LIMIT 100;";
            }
            
            ResultSet result = session.execute(query);
            if (!result.isSucceeded()) {
                throw new CoreException("查询节点失败: " + result.getErrorMessage());
            }
            
            List<Map<String, Object>> vertices = new ArrayList<>();
            for (int i = 0; i < result.rowsSize(); i++) {
                ValueWrapper vertexWrapper = result.rowValues(i).get(0);
                if (vertexWrapper.isVertex()) {
                    Map<String, Object> vertex = new HashMap<>();
                    // 解析顶点数据
                    // 实际实现中需要更复杂的解析逻辑
                    vertices.add(vertex);
                }
            }
            
            return vertices;
        } catch (Exception e) {
            throw new CoreException("查询Nebula节点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> queryEdges(String graphName, String label) {
        if (!isConnected()) {
            throw new CoreException("NebulaGraph连接未建立");
        }
        
        try {
            String query;
            if (label != null && !label.isEmpty()) {
                query = "MATCH ()-[e:`" + label + "`]->() RETURN e LIMIT 100;";
            } else {
                query = "MATCH ()-[e]->() RETURN e LIMIT 100;";
            }
            
            ResultSet result = session.execute(query);
            if (!result.isSucceeded()) {
                throw new CoreException("查询边失败: " + result.getErrorMessage());
            }
            
            List<Map<String, Object>> edges = new ArrayList<>();
            for (int i = 0; i < result.rowsSize(); i++) {
                ValueWrapper edgeWrapper = result.rowValues(i).get(0);
                if (edgeWrapper.isEdge()) {
                    Map<String, Object> edge = new HashMap<>();
                    // 解析边数据
                    // 实际实现中需要更复杂的解析逻辑
                    edges.add(edge);
                }
            }
            
            return edges;
        } catch (Exception e) {
            throw new CoreException("查询Nebula边失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String importFromCsv(String graphName, CsvImportConfig config, InputStream csvStream) {
        // NebulaGraph有专门的导入工具，这里实现基本逻辑
        try {
            // 在实际实现中，这里会：
            // 1. 将CSV流保存为临时文件
            // 2. 使用Nebula Importer工具导入数据
            // 3. 根据导入模式创建节点和边
            
            // 模拟实现
            switch (config.getImportMode()) {
                case VERTICES_ONLY:
                    return "NebulaGraph CSV导入功能：使用Nebula Importer导入节点数据";
                case EDGES_ONLY:
                    return "NebulaGraph CSV导入功能：使用Nebula Importer导入边数据";
                case VERTICES_AND_EDGES:
                    return "NebulaGraph CSV导入功能：使用Nebula Importer导入节点和边数据";
                default:
                    return "CSV导入失败：未知的导入模式";
            }
        } catch (Exception e) {
            throw new CoreException("CSV导入失败: " + e.getMessage(), e);
        }
    }
    
    // 辅助方法：格式化值用于nGQL查询
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "\"" + value.toString() + "\"";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return value.toString();
        } else {
            return "\"" + value.toString() + "\"";
        }
    }
    
    @Override
    public List<String> getGraphs(ConnectionConfig config) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            ResultSet result = session.execute("SHOW SPACES");
            if (!result.isSucceeded()) {
                throw new CoreException("获取图空间列表失败: " + result.getErrorMessage());
            }
            
            List<String> graphs = new ArrayList<>();
            for (int i = 0; i < result.rowsSize(); i++) {
                String spaceName = result.rowValues(i).get(0).asString();
                graphs.add(spaceName);
            }
            
            return graphs;
        } catch (Exception e) {
            throw new CoreException("获取Nebula图列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public GraphSchema getGraphSchema(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            GraphSchema schema = new GraphSchema();
            schema.setGraphName(graphName);
            schema.setDatabaseType("NEBULA");
            
            // 获取标签列表
            ResultSet tagResult = session.execute("SHOW TAGS");
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < tagResult.rowsSize(); i++) {
                tags.add(tagResult.rowValues(i).get(0).asString());
            }
            // 将String列表转换为LabelType列表
            List<LabelType> vertexLabelTypes = tags.stream().map(tag -> {
                LabelType labelType = new LabelType();
                labelType.setName(tag);
                labelType.setType("VERTEX");
                labelType.setDescription("NebulaGraph标签: " + tag);
                return labelType;
            }).collect(java.util.stream.Collectors.toList());
            schema.setVertexLabels(vertexLabelTypes);
            
            // 获取边类型列表
            ResultSet edgeResult = session.execute("SHOW EDGES");
            List<String> edges = new ArrayList<>();
            for (int i = 0; i < edgeResult.rowsSize(); i++) {
                edges.add(edgeResult.rowValues(i).get(0).asString());
            }
            // 将String列表转换为LabelType列表
            List<LabelType> edgeLabelTypes = edges.stream().map(edge -> {
                LabelType labelType = new LabelType();
                labelType.setName(edge);
                labelType.setType("EDGE");
                labelType.setDescription("NebulaGraph边类型: " + edge);
                return labelType;
            }).collect(java.util.stream.Collectors.toList());
            schema.setEdgeLabels(edgeLabelTypes);
            
            return schema;
        } catch (Exception e) {
            throw new CoreException("获取Nebula图schema失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void createGraph(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // 创建图空间
            String createSpaceQuery = "CREATE SPACE IF NOT EXISTS `" + graphName + "`(partition_num=10, replica_factor=1);";
            ResultSet result = session.execute(createSpaceQuery);
            if (!result.isSucceeded()) {
                throw new CoreException("创建图空间失败: " + result.getErrorMessage());
            }
            
            // 使用新创建的图空间
            session.execute("USE `" + graphName + "`");
            currentGraph = graphName;
            System.out.println("创建Nebula图空间: " + graphName);
        } catch (Exception e) {
            throw new CoreException("创建Nebula图失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteGraph(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            String dropSpaceQuery = "DROP SPACE IF EXISTS `" + graphName + "`;";
            ResultSet result = session.execute(dropSpaceQuery);
            if (!result.isSucceeded()) {
                throw new CoreException("删除图空间失败: " + result.getErrorMessage());
            }
            
            if (currentGraph != null && currentGraph.equals(graphName)) {
                currentGraph = null;
            }
            System.out.println("删除Nebula图空间: " + graphName);
        } catch (Exception e) {
            throw new CoreException("删除Nebula图失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void createVertexType(ConnectionConfig config, String graphName, LabelType labelType) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // 创建标签
            String createTagQuery = "CREATE TAG IF NOT EXISTS `" + labelType.getName() + "`();";
            ResultSet result = session.execute(createTagQuery);
            if (!result.isSucceeded()) {
                throw new CoreException("创建标签失败: " + result.getErrorMessage());
            }
            System.out.println("创建Nebula标签: " + labelType.getName());
        } catch (Exception e) {
            throw new CoreException("创建Nebula点类型失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteVertexType(ConnectionConfig config, String graphName, String labelName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            String dropTagQuery = "DROP TAG IF EXISTS `" + labelName + "`;";
            ResultSet result = session.execute(dropTagQuery);
            if (!result.isSucceeded()) {
                throw new CoreException("删除标签失败: " + result.getErrorMessage());
            }
            System.out.println("删除Nebula标签: " + labelName);
        } catch (Exception e) {
            throw new CoreException("删除Nebula点类型失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void createEdgeType(ConnectionConfig config, String graphName, LabelType labelType) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // 创建边类型
            String createEdgeQuery = "CREATE EDGE IF NOT EXISTS `" + labelType.getName() + "`();";
            ResultSet result = session.execute(createEdgeQuery);
            if (!result.isSucceeded()) {
                throw new CoreException("创建边类型失败: " + result.getErrorMessage());
            }
            System.out.println("创建Nebula边类型: " + labelType.getName());
        } catch (Exception e) {
            throw new CoreException("创建Nebula边类型失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteEdgeType(ConnectionConfig config, String graphName, String labelName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            String dropEdgeQuery = "DROP EDGE IF EXISTS `" + labelName + "`;";
            ResultSet result = session.execute(dropEdgeQuery);
            if (!result.isSucceeded()) {
                throw new CoreException("删除边类型失败: " + result.getErrorMessage());
            }
            System.out.println("删除Nebula边类型: " + labelName);
        } catch (Exception e) {
            throw new CoreException("删除Nebula边类型失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<LabelType> getVertexTypes(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            ResultSet result = session.execute("SHOW TAGS");
            if (!result.isSucceeded()) {
                throw new CoreException("获取标签列表失败: " + result.getErrorMessage());
            }
            
            List<LabelType> vertexTypes = new ArrayList<>();
            for (int i = 0; i < result.rowsSize(); i++) {
                String tagName = result.rowValues(i).get(0).asString();
                LabelType labelType = new LabelType();
                labelType.setName(tagName);
                labelType.setType("VERTEX");
                labelType.setDescription("NebulaGraph标签: " + tagName);
                vertexTypes.add(labelType);
            }
            
            return vertexTypes;
        } catch (Exception e) {
            throw new CoreException("获取Nebula点类型列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<LabelType> getEdgeTypes(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            ResultSet result = session.execute("SHOW EDGES");
            if (!result.isSucceeded()) {
                throw new CoreException("获取边类型列表失败: " + result.getErrorMessage());
            }
            
            List<LabelType> edgeTypes = new ArrayList<>();
            for (int i = 0; i < result.rowsSize(); i++) {
                String edgeName = result.rowValues(i).get(0).asString();
                LabelType labelType = new LabelType();
                labelType.setName(edgeName);
                labelType.setType("EDGE");
                labelType.setDescription("NebulaGraph边类型: " + edgeName);
                edgeTypes.add(labelType);
            }
            
            return edgeTypes;
        } catch (Exception e) {
            throw new CoreException("获取Nebula边类型列表失败: " + e.getMessage(), e);
        }
    }
    
    // SchemaHandler 接口实现
    @Override
    public Map<String, Map<String, Object>> getNodeTypes(String graphName) {
        Map<String, Map<String, Object>> nodeTypes = new HashMap<>();
        try {
            List<LabelType> vertexTypes = getVertexTypes(null, graphName);
            for (LabelType labelType : vertexTypes) {
                nodeTypes.put(labelType.getName(), new HashMap<>());
            }
        } catch (Exception e) {
            System.err.println("获取Nebula节点类型失败: " + e.getMessage());
        }
        return nodeTypes;
    }
    
    @Override
    public Map<String, Map<String, Object>> getEdgeTypes(String graphName) {
        Map<String, Map<String, Object>> edgeTypes = new HashMap<>();
        try {
            List<LabelType> edgeLabelTypes = getEdgeTypes(null, graphName);
            for (LabelType labelType : edgeLabelTypes) {
                edgeTypes.put(labelType.getName(), new HashMap<>());
            }
        } catch (Exception e) {
            System.err.println("获取Nebula边类型失败: " + e.getMessage());
        }
        return edgeTypes;
    }
    
    @Override
    public void createNodeType(String graphName, String typeName, Map<String, Object> properties) {
        try {
            LabelType labelType = new LabelType();
            labelType.setName(typeName);
            createVertexType(null, graphName, labelType);
        } catch (Exception e) {
            throw new CoreException("创建Nebula节点类型失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteNodeType(String graphName, String typeName) {
        try {
            deleteVertexType(null, graphName, typeName);
        } catch (Exception e) {
            throw new CoreException("删除Nebula节点类型失败: " + e.getMessage(), e);
        }
    }
    
    // ========== 新增方法实现 ==========
    
    @Override
    public Map<String, Object> updateVertex(String graphName, String uid, Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("NebulaGraph连接未建立");
        }
        
        try {
            // 构建更新节点的nGQL查询
            StringBuilder queryBuilder = new StringBuilder("UPDATE VERTEX \"" + uid + "\" SET ");
            
            if (properties != null && !properties.isEmpty()) {
                boolean first = true;
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    if (!first) queryBuilder.append(", ");
                    queryBuilder.append(entry.getKey()).append(" = ").append(formatValue(entry.getValue()));
                    first = false;
                }
            }
            queryBuilder.append(";");
            
            ResultSet result = session.execute(queryBuilder.toString());
            if (!result.isSucceeded()) {
                throw new CoreException("更新节点失败: " + result.getErrorMessage());
            }
            
            // 构建返回的更新后节点数据
            Map<String, Object> vertex = new HashMap<>();
            vertex.put("uid", uid);
            vertex.put("properties", properties != null ? properties : new HashMap<>());
            
            return vertex;
        } catch (Exception e) {
            throw new CoreException("更新Nebula节点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> updateEdge(String graphName, String uid, Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("NebulaGraph连接未建立");
        }
        
        try {
            // NebulaGraph边更新需要先查询边信息，这里简化实现
            // 实际实现中需要更复杂的逻辑
            StringBuilder queryBuilder = new StringBuilder("UPDATE EDGE SET ");
            
            if (properties != null && !properties.isEmpty()) {
                boolean first = true;
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    if (!first) queryBuilder.append(", ");
                    queryBuilder.append(entry.getKey()).append(" = ").append(formatValue(entry.getValue()));
                    first = false;
                }
            }
            
            // 简化的查询，实际需要更精确的边定位
            queryBuilder.append(" WHERE src() == \"" + uid + "\";");
            
            ResultSet result = session.execute(queryBuilder.toString());
            if (!result.isSucceeded()) {
                throw new CoreException("更新边失败: " + result.getErrorMessage());
            }
            
            // 构建返回的更新后边数据
            Map<String, Object> edge = new HashMap<>();
            edge.put("uid", uid);
            edge.put("properties", properties != null ? properties : new HashMap<>());
            
            return edge;
        } catch (Exception e) {
            throw new CoreException("更新Nebula边失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Object executeNativeQuery(ConnectionConfig config, String graphName, 
                                    String queryLanguage, String queryStatement) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // 验证查询语言
            if (!"nGQL".equalsIgnoreCase(queryLanguage) && !"GQL".equalsIgnoreCase(queryLanguage)) {
                throw new CoreException("NebulaGraph只支持nGQL/GQL查询语言");
            }
            
            // 切换到指定图空间
            if (graphName != null && !graphName.isEmpty()) {
                session.execute("USE `" + graphName + "`");
                currentGraph = graphName;
            }
            
            // 执行原生查询
            ResultSet result = session.execute(queryStatement);
            
            // 处理查询结果
            List<Map<String, Object>> rows = new ArrayList<>();
            if (result.isSucceeded()) {
                // 获取列名
                List<String> columnNames = result.getColumnNames();
                
                for (int i = 0; i < result.rowsSize(); i++) {
                    Map<String, Object> row = new HashMap<>();
                    List<ValueWrapper> rowValues = result.rowValues(i).values(); // 获取一行的所有列值
                    for (int j = 0; j < rowValues.size(); j++) {
                        String columnName = j < columnNames.size() ? columnNames.get(j) : "col_" + j; // 使用真实的列名，如果获取不到则使用默认名称
                        ValueWrapper value = rowValues.get(j);
                        
                        try {
                            if (value.isString()) {
                                row.put(columnName, value.asString());
                            } else if (value.isLong()) {
                                row.put(columnName, value.asLong());
                            } else if (value.isDouble()) {
                                row.put(columnName, value.asDouble());
                            } else if (value.isBoolean()) {
                                row.put(columnName, value.asBoolean());
                            } else if (value.isVertex()) {
                                row.put(columnName, value.asNode().toString());
                            } else if (value.isEdge()) {
                                row.put(columnName, value.asRelationship().toString());
                            } else if (value.isPath()) {
                                row.put(columnName, value.asPath().toString());
                            } else {
                                row.put(columnName, value.toString());
                            }
                        } catch (Exception e) {
                            // 如果类型转换失败，使用字符串形式
                            row.put(columnName, value.toString());
                        }
                    }
                    rows.add(row);
                }
            }
            
            Map<String, Object> queryResult = new HashMap<>();
            queryResult.put("success", result.isSucceeded());
            queryResult.put("rows", rows);
            queryResult.put("rowCount", rows.size());
            queryResult.put("errorMessage", result.isSucceeded() ? null : result.getErrorMessage());
            queryResult.put("queryLanguage", "nGQL");
            
            return queryResult;
        } catch (Exception e) {
            throw new CoreException("执行NebulaGraph原生查询失败: " + e.getMessage(), e);
        }
    }
}
