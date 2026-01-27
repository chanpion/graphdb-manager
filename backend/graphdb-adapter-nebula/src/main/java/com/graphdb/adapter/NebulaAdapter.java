package com.graphdb.adapter;

import com.graphdb.core.constant.DatabaseTypeEnum;
import com.graphdb.core.exception.CoreException;
import com.graphdb.core.interfaces.DataHandler;
import com.graphdb.core.interfaces.GraphAdapter;
import com.graphdb.core.interfaces.SchemaHandler;
import com.graphdb.core.model.ConnectionConfig;
import com.graphdb.core.model.CsvImportConfig;
import com.graphdb.core.model.Edge;
import com.graphdb.core.model.GraphQueryResult;
import com.graphdb.core.model.GraphSchema;
import com.graphdb.core.model.LabelType;
import com.graphdb.core.model.Vertex;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.Node;
import com.vesoft.nebula.client.graph.data.PathWrapper;
import com.vesoft.nebula.client.graph.data.Relationship;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                    if (!first) {
                        queryBuilder.append(",");
                    }
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
                    if (!first) {
                        queryBuilder.append(",");
                    }
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
            // NebulaGraph删除边需要知道源节点、目标节点和边类型
            // 假设uid格式为 "edge_edgeType_sourceVertexId_targetVertexId"
            if (uid != null && uid.startsWith("edge_")) {
                String[] parts = uid.substring(5).split("_", 3);
                if (parts.length == 3) {
                    String edgeType = parts[0];
                    String sourceId = parts[1];
                    String targetId = parts[2];

                    // 删除指定边
                    String query = String.format("DELETE EDGE `%s` \"%s\"->\"%s\";", edgeType, sourceId, targetId);
                    ResultSet result = session.execute(query);
                    if (!result.isSucceeded()) {
                        throw new CoreException("删除边失败: " + result.getErrorMessage());
                    }
                }
            }
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
                    if (!first) {
                        queryBuilder.append(", ");
                    }
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
                    if (!first) {
                        queryBuilder.append(", ");
                    }
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
    public GraphQueryResult executeNativeQuery(ConnectionConfig config, String graphName,
                                               String queryLanguage, String queryStatement) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }

        GraphQueryResult result = new GraphQueryResult();

        try {
            // 验证查询语言
            if (!"nGQL".equalsIgnoreCase(queryLanguage) && !"GQL".equalsIgnoreCase(queryLanguage) &&
                    !"NGQL".equalsIgnoreCase(queryLanguage)) {
                throw new CoreException("NebulaGraph只支持nGQL/GQL/NGQL查询语言");
            }

            // 切换到指定图空间
            if (graphName != null && !graphName.isEmpty()) {
                session.execute("USE `" + graphName + "`");
                currentGraph = graphName;
            }

            // 执行原生查询
            ResultSet nebulaResult = session.execute(queryStatement);

            if (!nebulaResult.isSucceeded()) {
                throw new CoreException("Nebula查询执行失败: " + nebulaResult.getErrorMessage());
            }

            // 处理查询结果，提取节点和边
            List<Vertex> vertices = new ArrayList<>();
            List<Edge> edges = new ArrayList<>();

            // 获取列名
            List<String> columnNames = nebulaResult.getColumnNames();

            for (int i = 0; i < nebulaResult.rowsSize(); i++) {
                List<ValueWrapper> rowValues = nebulaResult.rowValues(i).values();

                for (int j = 0; j < rowValues.size(); j++) {
                    ValueWrapper value = rowValues.get(j);
                    extractNebulaGraphElements(value, vertices, edges);
                }
            }

            // 设置结果
            result.setVertices(vertices);
            result.setEdges(edges);

            // 设置统计信息
            GraphQueryResult.QueryStatistics stats = new GraphQueryResult.QueryStatistics();
            stats.setResultRows(vertices.size() + edges.size());
            stats.setStatus("SUCCESS");
            result.setStatistics(stats);

            return result;

        } catch (Exception e) {
            // 设置错误信息
            GraphQueryResult.QueryStatistics stats = new GraphQueryResult.QueryStatistics();
            stats.setStatus("ERROR");
            stats.setErrorMessage(e.getMessage());
            result.setStatistics(stats);

            throw new CoreException("执行NebulaGraph原生查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从Nebula查询结果中提取图元素（节点和边）
     */
    private void extractNebulaGraphElements(ValueWrapper value, List<Vertex> vertices, List<Edge> edges) {
        try {
            if (value.isVertex()) {
                // 处理节点
                Node nebulaVertex = value.asNode();
                com.graphdb.core.model.Vertex vertex = convertNebulaVertexToVertex(nebulaVertex);
                if (!containsVertex(vertices, vertex.getUid())) {
                    vertices.add(vertex);
                }
            } else if (value.isEdge()) {
                // 处理边
                Relationship nebulaEdge = value.asRelationship();
                com.graphdb.core.model.Edge edge = convertNebulaEdgeToEdge(nebulaEdge);
                if (!containsEdge(edges, edge.getUid())) {
                    edges.add(edge);
                }
            } else if (value.isPath()) {
                // 处理路径，提取路径中的节点和边
                PathWrapper path = value.asPath();
                extractFromNebulaPath(path, vertices, edges);
            }
        } catch (Exception e) {
            // 忽略类型转换异常，继续处理其他值
        }
    }

    /**
     * 从Nebula路径中提取节点和边
     */
    private void extractFromNebulaPath(PathWrapper path,
                                       List<Vertex> vertices, List<Edge> edges) {
        try {
            // 提取路径中的节点
            List<Node> nodes = path.getNodes();
            nodes.forEach(node -> {
                Vertex vertex = convertNebulaVertexToVertex(node);
                vertices.add(vertex);
            });

            // 提取路径中的边
            List<Relationship> relationships = path.getRelationships();
            relationships.forEach(relationship -> {
                Edge edge = convertNebulaEdgeToEdge(relationship);
                edges.add(edge);
            });
        } catch (Exception e) {
            // 忽略路径处理异常
        }
    }

    /**
     * 将Nebula节点转换为Vertex对象
     */
    private com.graphdb.core.model.Vertex convertNebulaVertexToVertex(Node nebulaVertex) {
        com.graphdb.core.model.Vertex vertex = new com.graphdb.core.model.Vertex();

        try {
            ;
            // 设置节点ID
            vertex.setUid(nebulaVertex.getId().asString());

            // 设置标签（使用第一个标签）
            if (!nebulaVertex.tagNames().isEmpty()) {
                vertex.setLabel(nebulaVertex.tagNames().get(0));
            }

            // 设置属性
            Map<String, ValueWrapper> p = nebulaVertex.properties(vertex.getLabel());
            Map<String, Object> properties = convertValueWrapperMap(p);
            vertex.setProperties(properties);

        } catch (Exception e) {
            // 设置默认值
            vertex.setUid("unknown_vertex");
            vertex.setLabel("Vertex");
        }

        return vertex;
    }

    /**
     * 将Nebula边转换为Edge对象
     */
    private com.graphdb.core.model.Edge convertNebulaEdgeToEdge(Relationship nebulaEdge) {
        com.graphdb.core.model.Edge edge = new com.graphdb.core.model.Edge();

        try {
            // 设置边ID（使用源节点-类型-目标节点组合）
            String edgeId = nebulaEdge.srcId().toString() + "_" +
                    nebulaEdge.edgeName() + "_" +
                    nebulaEdge.dstId().toString();
            edge.setUid(edgeId);

            edge.setLabel(nebulaEdge.edgeName());
            edge.setSourceUid(nebulaEdge.srcId().toString());
            edge.setTargetUid(nebulaEdge.dstId().toString());

            // 设置属性
            Map<String, ValueWrapper> p = nebulaEdge.properties();
            Map<String, Object> properties = convertValueWrapperMap(p);
            edge.setProperties(properties);

        } catch (Exception e) {
            // 设置默认值
            edge.setUid("unknown_edge");
            edge.setLabel("Edge");
            edge.setSourceUid("unknown");
            edge.setTargetUid("unknown");
        }

        return edge;
    }

    /**
     * 检查节点是否已存在
     */
    private boolean containsVertex(List<Vertex> vertices, String uid) {
        return vertices.stream().anyMatch(v -> uid.equals(v.getUid()));
    }

    /**
     * 检查边是否已存在
     */
    private boolean containsEdge(List<Edge> edges, String uid) {
        return edges.stream().anyMatch(e -> uid.equals(e.getUid()));
    }

    @Override
    public Map<String, Object> executeNativeQuery(String graphName, String query, DatabaseTypeEnum dbType) {
        try {
            // NebulaGraph使用nGQL查询语言
            System.out.println("执行nGQL查询: " + query);

            // 根据查询类型返回不同的统计信息
            if (query.contains("count")) {
                // 节点数量统计
                String vertexCountQuery = "MATCH (v) RETURN count(v) AS count";
                ResultSet result = session.execute(vertexCountQuery);
                long vertexCount = 0L;
                if (result.isSucceeded() && result.rowsSize() > 0) {
                    try {
                        ValueWrapper value = result.rowValues(0).get(0);
                        vertexCount = value.asLong();
                    } catch (Exception e) {
                        System.out.println("获取节点数量失败: " + e.getMessage());
                    }
                }
                return Map.of("vertexCount", vertexCount);

            } else if (query.contains("degreeDistribution")) {
                // 度分布统计
                String degreeQuery = "MATCH (v) RETURN v.degree AS degree ORDER BY degree DESC LIMIT 100";
                ResultSet result = session.execute(degreeQuery);
                Map<String, Long> distribution = new HashMap<>();
                if (result.isSucceeded() && result.rowsSize() > 0) {
                    for (int i = 0; i < result.rowsSize(); i++) {
                        try {
                            List<ValueWrapper> row = result.rowValues(i).values();
                            if (!row.isEmpty()) {
                                ValueWrapper degreeValue = row.get(0);
                                Long degree = degreeValue.asLong();
                                distribution.put(String.valueOf(degree), distribution.getOrDefault(String.valueOf(degree), 0L) + 1);
                            }
                        } catch (Exception e) {
                            System.out.println("处理度分布数据失败: " + e.getMessage());
                        }
                    }
                }
                return Map.of("degreeDistribution", distribution);

            } else if (query.contains("edgeTypeDistribution")) {
                // 边类型分布统计
                String edgeTypeQuery = "MATCH ()-[r]->(r) RETURN type(r) AS edgeType, count(r) AS count ORDER BY count DESC";
                ResultSet result = session.execute(edgeTypeQuery);
                Map<String, Long> distribution = new HashMap<>();
                if (result.isSucceeded() && result.rowsSize() > 0) {
                    for (int i = 0; i < result.rowsSize(); i++) {
                        try {
                            List<ValueWrapper> row = result.rowValues(i).values();
                            if (row.size() >= 2) {
                                String type = row.get(0).asString();
                                Long count = row.get(1).asLong();
                                distribution.put(type, count);
                            }
                        } catch (Exception e) {
                            System.out.println("处理边类型分布数据失败: " + e.getMessage());
                        }
                    }
                }
                return Map.of("edgeTypeDistribution", distribution);

            } else {
                // 通用查询执行
                ResultSet result = session.execute(query);
                List<Map<String, Object>> results = new ArrayList<>();

                if (result.isSucceeded() && result.rowsSize() > 0) {
                    List<String> columnNames = result.getColumnNames();

                    for (int i = 0; i < result.rowsSize(); i++) {
                        try {
                            List<ValueWrapper> row = result.rowValues(i).values();
                            Map<String, Object> rowData = new HashMap<>();

                            for (int j = 0; j < row.size() && j < columnNames.size(); j++) {
                                ValueWrapper valueWrapper = row.get(j);
                                String columnName = columnNames.get(j);

                                Object value;
                                if (valueWrapper.isString()) {
                                    value = valueWrapper.asString();
                                } else if (valueWrapper.isLong()) {
                                    value = valueWrapper.asLong();
                                } else if (valueWrapper.isDouble()) {
                                    value = valueWrapper.asDouble();
                                } else if (valueWrapper.isBoolean()) {
                                    value = valueWrapper.asBoolean();
                                } else {
                                    value = valueWrapper.toString();
                                }
                                rowData.put(columnName, value);
                            }
                            results.add(rowData);
                        } catch (Exception e) {
                            System.out.println("处理查询结果行失败: " + e.getMessage());
                        }
                    }
                }

                return Map.of(
                        "resultCount", result.isSucceeded() ? result.rowsSize() : 0,
                        "results", results
                );
            }
        } catch (Exception e) {
            System.out.println("nGQL查询失败: " + e.getMessage());
            throw new CoreException("执行nGQL查询失败: " + e.getMessage(), e);
        }
    }


    public Map<String, Object> convertValueWrapperMap(Map<String, ValueWrapper> wrapperMap) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, ValueWrapper> entry : wrapperMap.entrySet()) {
            result.put(entry.getKey(), convertValueWrapper(entry.getValue()));
        }
        return result;
    }

    private Object convertValueWrapper(ValueWrapper value) {
        try {


            // 按常见类型进行判断和转换
            if (value.isNull()) {
                return null;
            } else if (value.isBoolean()) {
                return value.asBoolean();
            } else if (value.isLong()) {
                return value.asLong();
            } else if (value.isDouble()) {
                return value.asDouble();
            } else if (value.isString()) {
                return value.asString();
            } else if (value.isDate()) {
                return value.asDate();
            } else if (value.isTime()) {
                return value.asTime();
            } else if (value.isDateTime()) {
                return value.asDateTime();
            } else if (value.isList()) {
                // 递归处理列表
                return value.asList().stream()
                        .map(this::convertValueWrapper)
                        .collect(Collectors.toList());
            } else if (value.isSet()) {
                // 递归处理集合
                return value.asSet().stream()
                        .map(this::convertValueWrapper)
                        .collect(Collectors.toSet());
            } else if (value.isMap()) {
                // 递归处理Map，注意键可能是ValueWrapper
                Map<String, Object> map = new HashMap<>();
                value.asMap().forEach((k, v) -> map.put(k, convertValueWrapper(v)));
                return map;
            } else {
                // 对于未知或复杂类型（如Vertex, Edge），直接返回ValueWrapper
                // 或者根据需求转为特定对象
                return value;
            }
        } catch (Exception e) {

            return value;
        }

    }
}
