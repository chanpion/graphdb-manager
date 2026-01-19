package com.graphdb.adapter.neo4j;

import com.graphdb.core.interfaces.GraphAdapter;
import com.graphdb.core.interfaces.DataHandler;
import com.graphdb.core.constant.DatabaseTypeEnum;
import com.graphdb.core.model.ConnectionConfig;
import com.graphdb.core.model.GraphSchema;
import com.graphdb.core.model.LabelType;
import com.graphdb.core.model.CsvImportConfig;
import com.graphdb.core.exception.CoreException;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Neo4j适配器实现
 */
@Component
public class Neo4jAdapter implements GraphAdapter, DataHandler {
    
    private Driver driver;
    private Session session;
    private boolean connected = false;
    
    @Override
    public DatabaseTypeEnum getDatabaseType() {
        return DatabaseTypeEnum.NEO4J;
    }
    
    @Override
    public boolean testConnection(ConnectionConfig config) throws CoreException {
        try (Driver testDriver = createDriver(config);
             Session testSession = testDriver.session()) {
            
            // 执行简单的查询测试连接
            Result result = testSession.run("RETURN 1 AS test");
            return result.single().get("test").asInt() == 1;
        } catch (Exception e) {
            throw new CoreException("Neo4j连接测试失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void connect(ConnectionConfig config) throws CoreException {
        try {
            driver = createDriver(config);
            session = driver.session();
            connected = true;
            System.out.println("成功连接到Neo4j: " + config.getHost() + ":" + config.getPort());
        } catch (Exception e) {
            throw new CoreException("Neo4j连接失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void disconnect() {
        if (session != null) {
            session.close();
            session = null;
        }
        if (driver != null) {
            driver.close();
            driver = null;
        }
        connected = false;
        System.out.println("已断开Neo4j连接");
    }
    
    @Override
    public boolean isConnected() {
        return connected && driver != null && session != null;
    }
    
    private Driver createDriver(ConnectionConfig config) {
        String uri = "bolt://" + config.getHost() + ":" + config.getPort();
        AuthToken auth = AuthTokens.basic(config.getUsername(), config.getPassword());
        return GraphDatabase.driver(uri, auth);
    }
    
    @Override
    public List<String> getGraphs(ConnectionConfig config) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // Neo4j默认只有一个数据库，返回当前数据库名称
            String databaseName = config.getDatabaseName() != null ? config.getDatabaseName() : "neo4j";
            return List.of(databaseName);
        } catch (Exception e) {
            throw new CoreException("获取Neo4j图列表失败: " + e.getMessage(), e);
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
            schema.setDatabaseType("NEO4J");
            
            // 获取节点标签列表
            String nodeLabelsQuery = "CALL db.labels() YIELD label RETURN label";
            Result result = session.run(nodeLabelsQuery);
            List<String> nodeLabels = result.list(record -> record.get("label").asString());
            // 将String列表转换为LabelType列表
            List<LabelType> vertexLabelTypes = nodeLabels.stream().map(label -> {
                LabelType labelType = new LabelType();
                labelType.setName(label);
                labelType.setType("VERTEX");
                labelType.setDescription("Neo4j节点标签: " + label);
                return labelType;
            }).collect(java.util.stream.Collectors.toList());
            schema.setVertexLabels(vertexLabelTypes);
            
            // 获取关系类型列表
            String relTypesQuery = "CALL db.relationshipTypes() YIELD relationshipType RETURN relationshipType";
            result = session.run(relTypesQuery);
            List<String> relTypes = result.list(record -> record.get("relationshipType").asString());
            // 将String列表转换为LabelType列表
            List<LabelType> edgeLabelTypes = relTypes.stream().map(relType -> {
                LabelType labelType = new LabelType();
                labelType.setName(relType);
                labelType.setType("EDGE");
                labelType.setDescription("Neo4j关系类型: " + relType);
                return labelType;
            }).collect(java.util.stream.Collectors.toList());
            schema.setEdgeLabels(edgeLabelTypes);
            
            return schema;
        } catch (Exception e) {
            throw new CoreException("获取Neo4j图schema失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void createGraph(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // Neo4j 5.x+ 支持多数据库
            String createDbQuery = "CREATE DATABASE $name IF NOT EXISTS";
            session.run(createDbQuery, Values.parameters("name", graphName));
            System.out.println("创建Neo4j数据库: " + graphName);
        } catch (Exception e) {
            throw new CoreException("创建Neo4j图失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteGraph(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // Neo4j 5.x+ 支持多数据库
            String dropDbQuery = "DROP DATABASE $name IF EXISTS";
            session.run(dropDbQuery, Values.parameters("name", graphName));
            System.out.println("删除Neo4j数据库: " + graphName);
        } catch (Exception e) {
            throw new CoreException("删除Neo4j图失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void createVertexType(ConnectionConfig config, String graphName, LabelType labelType) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // Neo4j中节点标签是隐式创建的，只需创建带有该标签的节点即可
            String createNodeQuery = "CREATE (n:$label {name: $labelName})";
            session.run(createNodeQuery, Values.parameters("label", labelType.getName(), "labelName", labelType.getName()));
            session.run("MATCH (n) WHERE n.name = $labelName DELETE n", Values.parameters("labelName", labelType.getName()));
            System.out.println("创建Neo4j节点类型: " + labelType.getName());
        } catch (Exception e) {
            throw new CoreException("创建Neo4j点类型失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteVertexType(ConnectionConfig config, String graphName, String labelName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // 删除所有带有该标签的节点
            String deleteNodesQuery = "MATCH (n:$label) DELETE n";
            session.run(deleteNodesQuery, Values.parameters("label", labelName));
            System.out.println("删除Neo4j节点类型: " + labelName);
        } catch (Exception e) {
            throw new CoreException("删除Neo4j点类型失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void createEdgeType(ConnectionConfig config, String graphName, LabelType labelType) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // Neo4j中关系类型是隐式创建的，只需创建带有该类型的关系即可
            String createEdgeQuery = "CREATE (a:TestNode)-[r:$type {name: $typeName}]->(b:TestNode)";
            session.run(createEdgeQuery, Values.parameters("type", labelType.getName(), "typeName", labelType.getName()));
            session.run("MATCH (a:TestNode)-[r]->(b:TestNode) DELETE a, r, b");
            System.out.println("创建Neo4j边类型: " + labelType.getName());
        } catch (Exception e) {
            throw new CoreException("创建Neo4j边类型失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteEdgeType(ConnectionConfig config, String graphName, String labelName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // 删除所有带有该类型的关系
            String deleteEdgesQuery = "MATCH ()-[r:$type]->() DELETE r";
            session.run(deleteEdgesQuery, Values.parameters("type", labelName));
            System.out.println("删除Neo4j边类型: " + labelName);
        } catch (Exception e) {
            throw new CoreException("删除Neo4j边类型失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<LabelType> getVertexTypes(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            String query = "CALL db.labels() YIELD label RETURN label";
            Result result = session.run(query);
            
            List<LabelType> vertexTypes = new ArrayList<>();
            for (Record record : result.list()) {
                String labelName = record.get("label").asString();
                LabelType labelType = new LabelType();
                labelType.setName(labelName);
                labelType.setType("VERTEX");
                labelType.setDescription("Neo4j节点标签: " + labelName);
                vertexTypes.add(labelType);
            }
            
            return vertexTypes;
        } catch (Exception e) {
            throw new CoreException("获取Neo4j点类型列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<LabelType> getEdgeTypes(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            String query = "CALL db.relationshipTypes() YIELD relationshipType RETURN relationshipType";
            Result result = session.run(query);
            
            List<LabelType> edgeTypes = new ArrayList<>();
            for (Record record : result.list()) {
                String typeName = record.get("relationshipType").asString();
                LabelType labelType = new LabelType();
                labelType.setName(typeName);
                labelType.setType("EDGE");
                labelType.setDescription("Neo4j关系类型: " + typeName);
                edgeTypes.add(labelType);
            }
            
            return edgeTypes;
        } catch (Exception e) {
            throw new CoreException("获取Neo4j边类型列表失败: " + e.getMessage(), e);
        }
    }
    
    // ========== DataHandler 接口实现 ==========
    
    @Override
    public Map<String, Object> createVertex(String graphName, String label, Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("Neo4j连接未建立");
        }
        
        try {
            // 生成唯一ID
            String uid = "vertex_" + System.currentTimeMillis();
            
            // 构建创建节点的Cypher查询
            StringBuilder queryBuilder = new StringBuilder("CREATE (n:" + label + " {uid: $uid");
            
            if (properties != null && !properties.isEmpty()) {
                for (String key : properties.keySet()) {
                    queryBuilder.append(", ").append(key).append(": $prop_").append(key);
                }
            }
            queryBuilder.append("}) RETURN n");
            
            // 构建参数
            Map<String, Object> params = new HashMap<>();
            params.put("uid", uid);
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    params.put("prop_" + entry.getKey(), entry.getValue());
                }
            }
            
            Result result = session.run(queryBuilder.toString(), params);
            Record record = result.single();
            Node node = record.get("n").asNode();
            
            // 构建返回的节点数据
            Map<String, Object> vertex = new HashMap<>();
            vertex.put("uid", uid);
            vertex.put("label", label);
            vertex.put("properties", node.asMap());
            
            return vertex;
        } catch (Exception e) {
            throw new CoreException("创建Neo4j节点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> createEdge(String graphName, String label, 
                                          String sourceUid, String targetUid, 
                                          Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("Neo4j连接未建立");
        }
        
        try {
            // 生成唯一ID
            String uid = "edge_" + System.currentTimeMillis();
            
            // 构建创建边的Cypher查询
            StringBuilder queryBuilder = new StringBuilder("MATCH (a), (b) WHERE a.uid = $sourceUid AND b.uid = $targetUid ");
            queryBuilder.append("CREATE (a)-[r:").append(label).append(" {uid: $uid");
            
            if (properties != null && !properties.isEmpty()) {
                for (String key : properties.keySet()) {
                    queryBuilder.append(", ").append(key).append(": $prop_").append(key);
                }
            }
            queryBuilder.append("}]->(b) RETURN r");
            
            // 构建参数
            Map<String, Object> params = new HashMap<>();
            params.put("uid", uid);
            params.put("sourceUid", sourceUid);
            params.put("targetUid", targetUid);
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    params.put("prop_" + entry.getKey(), entry.getValue());
                }
            }
            
            Result result = session.run(queryBuilder.toString(), params);
            Record record = result.single();
            Relationship relationship = record.get("r").asRelationship();
            
            // 构建返回的边数据
            Map<String, Object> edge = new HashMap<>();
            edge.put("uid", uid);
            edge.put("label", label);
            edge.put("sourceUid", sourceUid);
            edge.put("targetUid", targetUid);
            edge.put("properties", relationship.asMap());
            
            return edge;
        } catch (Exception e) {
            throw new CoreException("创建Neo4j边失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteVertex(String graphName, String uid) {
        if (!isConnected()) {
            throw new CoreException("Neo4j连接未建立");
        }
        
        try {
            // 删除节点及其所有关系
            String query = "MATCH (n {uid: $uid}) DETACH DELETE n";
            session.run(query, Values.parameters("uid", uid));
        } catch (Exception e) {
            throw new CoreException("删除Neo4j节点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteEdge(String graphName, String uid) {
        if (!isConnected()) {
            throw new CoreException("Neo4j连接未建立");
        }
        
        try {
            // 删除边
            String query = "MATCH ()-[r {uid: $uid}]->() DELETE r";
            session.run(query, Values.parameters("uid", uid));
        } catch (Exception e) {
            throw new CoreException("删除Neo4j边失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> queryVertices(String graphName, String label) {
        if (!isConnected()) {
            throw new CoreException("Neo4j连接未建立");
        }
        
        try {
            String query;
            if (label != null && !label.isEmpty()) {
                query = "MATCH (n:" + label + ") RETURN n";
            } else {
                query = "MATCH (n) RETURN n";
            }
            
            Result result = session.run(query);
            List<Map<String, Object>> vertices = new ArrayList<>();
            
            for (Record record : result.list()) {
                Node node = record.get("n").asNode();
                Map<String, Object> vertex = new HashMap<>();
                
                // 获取节点标签
                List<String> labels = StreamSupport.stream(node.labels().spliterator(), false).collect(Collectors.toList());
                String primaryLabel = labels.isEmpty() ? "Unknown" : labels.get(0);
                
                vertex.put("uid", node.get("uid").asString());
                vertex.put("label", primaryLabel);
                vertex.put("properties", node.asMap());
                
                vertices.add(vertex);
            }
            
            return vertices;
        } catch (Exception e) {
            throw new CoreException("查询Neo4j节点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> queryEdges(String graphName, String label) {
        if (!isConnected()) {
            throw new CoreException("Neo4j连接未建立");
        }
        
        try {
            String query;
            if (label != null && !label.isEmpty()) {
                query = "MATCH ()-[r:" + label + "]->() RETURN r, startNode(r) as source, endNode(r) as target";
            } else {
                query = "MATCH ()-[r]->() RETURN r, startNode(r) as source, endNode(r) as target";
            }
            
            Result result = session.run(query);
            List<Map<String, Object>> edges = new ArrayList<>();
            
            for (Record record : result.list()) {
                Relationship relationship = record.get("r").asRelationship();
                Node source = record.get("source").asNode();
                Node target = record.get("target").asNode();
                
                Map<String, Object> edge = new HashMap<>();
                edge.put("uid", relationship.get("uid").asString());
                edge.put("label", relationship.type());
                edge.put("sourceUid", source.get("uid").asString());
                edge.put("targetUid", target.get("uid").asString());
                edge.put("properties", relationship.asMap());
                
                edges.add(edge);
            }
            
            return edges;
        } catch (Exception e) {
            throw new CoreException("查询Neo4j边失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String importFromCsv(String graphName, CsvImportConfig config, InputStream csvStream) {
        // Neo4j有专门的LOAD CSV功能，但需要文件路径，这里实现基本逻辑
        try {
            // 在实际实现中，这里会：
            // 1. 将CSV流保存为临时文件
            // 2. 使用LOAD CSV命令导入数据
            // 3. 根据导入模式创建节点和边
            
            // 模拟实现
            switch (config.getImportMode()) {
                case VERTICES_ONLY:
                    return "CSV导入功能：使用LOAD CSV导入节点数据";
                case EDGES_ONLY:
                    return "CSV导入功能：使用LOAD CSV导入边数据";
                case VERTICES_AND_EDGES:
                    return "CSV导入功能：使用LOAD CSV导入节点和边数据";
                default:
                    return "CSV导入失败：未知的导入模式";
            }
        } catch (Exception e) {
            throw new CoreException("CSV导入失败: " + e.getMessage(), e);
        }
    }
}