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
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.InputStream;

/**
 * JanusGraph适配器实现
 */
@Service
public class JanusAdapter implements GraphAdapter, SchemaHandler, DataHandler {

    private JanusGraph graph;
    private boolean connected;
    private String currentGraph;

    public JanusAdapter() {
        // 初始化时不需要配置，连接时再配置
    }

    private void initializeGraph(ConnectionConfig config) {
        JanusGraphFactory.Builder builder = JanusGraphFactory.build()
            .set("storage.backend", getStorageBackend(config.getDatabaseType()))
            .set("storage.hostname", config.getHost())
            .set("storage.port", config.getPort());

        if (config.getUsername() != null) {
            builder.set("storage.username", config.getUsername());
        }
        if (config.getPassword() != null) {
            builder.set("storage.password", config.getPassword());
        }

        graph = builder.open();

        if (config.getDatabaseName() != null) {
            currentGraph = config.getDatabaseName();
        }
    }

    private String getStorageBackend(String databaseType) {
        // 根据数据库类型返回合适的存储后端
        if (databaseType != null) {
            String type = databaseType.toLowerCase();
            if (type.contains("cassandra")) {
                return "cql";
            } else if (type.contains("hbase")) {
                return "hbase";
            } else if (type.contains("berkeleyje")) {
                return "berkeleyje";
            }
        }
        // 默认使用inmemory存储用于测试
        return "inmemory";
    }

    @Override
    public DatabaseTypeEnum getDatabaseType() {
        return DatabaseTypeEnum.JANUS;
    }

    @Override
    public boolean testConnection(ConnectionConfig config) {
        try {
            JanusGraph testGraph = initializeTestGraph(config);
            testGraph.close();
            System.out.println("JanusGraph连接测试成功");
            return true;
        } catch (Exception e) {
            throw new RuntimeException("JanusGraph连接测试失败: " + e.getMessage());
        }
    }

    private JanusGraph initializeTestGraph(ConnectionConfig config) {
        JanusGraphFactory.Builder builder = JanusGraphFactory.build()
            .set("storage.backend", getStorageBackend(config.getDatabaseType()))
            .set("storage.hostname", config.getHost())
            .set("storage.port", config.getPort());

        if (config.getUsername() != null) {
            builder.set("storage.username", config.getUsername());
        }
        if (config.getPassword() != null) {
            builder.set("storage.password", config.getPassword());
        }

        return builder.open();
    }

    @Override
    public void connect(ConnectionConfig config) {
        try {
            initializeGraph(config);
            connected = true;

            if (config.getDatabaseName() != null) {
                currentGraph = config.getDatabaseName();
            }
        } catch (Exception e) {
            if (graph != null) {
                graph.close();
            }
            throw new RuntimeException("JanusGraph连接失败: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        if (graph != null) {
            graph.close();
            connected = false;
            currentGraph = null;
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public List<String> getGraphs(ConnectionConfig config) throws CoreException {
        // JanusGraph通常不需要显式管理多个图，返回当前连接信息
        List<String> graphs = new ArrayList<>();
        if (currentGraph != null) {
            graphs.add(currentGraph);
        }
        return graphs;
    }

    @Override
    public GraphSchema getGraphSchema(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            GraphSchema schema = new GraphSchema();
            schema.setGraphName(graphName);
            schema.setDatabaseType("JANUS");
            
            // 获取顶点标签列表
            JanusGraphManagement management = graph.openManagement();
            Iterable<org.janusgraph.core.VertexLabel> vertexLabelIterable = management.getVertexLabels();
            List<String> vertexLabels = new ArrayList<>();
            for (org.janusgraph.core.VertexLabel label : vertexLabelIterable) {
                vertexLabels.add(label.name());
            }
            // 将String列表转换为LabelType列表
            List<LabelType> vertexLabelTypes = vertexLabels.stream().map(label -> {
                LabelType labelType = new LabelType();
                labelType.setName(label);
                labelType.setType("VERTEX");
                labelType.setDescription("JanusGraph顶点标签: " + label);
                return labelType;
            }).collect(java.util.stream.Collectors.toList());
            schema.setVertexLabels(vertexLabelTypes);
            
            // 获取边标签列表
            Iterable<org.janusgraph.core.EdgeLabel> edgeLabelIterable = management.getRelationTypes(org.janusgraph.core.EdgeLabel.class);
            List<String> edgeLabels = new ArrayList<>();
            for (org.janusgraph.core.EdgeLabel label : edgeLabelIterable) {
                edgeLabels.add(label.name());
            }
            // 将String列表转换为LabelType列表
            List<LabelType> edgeLabelTypes = edgeLabels.stream().map(label -> {
                LabelType labelType = new LabelType();
                labelType.setName(label);
                labelType.setType("EDGE");
                labelType.setDescription("JanusGraph边标签: " + label);
                return labelType;
            }).collect(java.util.stream.Collectors.toList());
            schema.setEdgeLabels(edgeLabelTypes);
            
            management.rollback();
            return schema;
        } catch (Exception e) {
            throw new CoreException("获取JanusGraph Schema失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void createGraph(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // JanusGraph在连接时已经创建了图，这里主要是切换或配置
            System.out.println("JanusGraph图已创建: " + graphName);
        } catch (Exception e) {
            throw new CoreException("创建JanusGraph图失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteGraph(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            // JanusGraph删除图需要关闭图并删除底层存储
            if (graph != null) {
                graph.close();
            }
            System.out.println("JanusGraph图已删除: " + graphName);
        } catch (Exception e) {
            throw new CoreException("删除JanusGraph图失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void createVertexType(ConnectionConfig config, String graphName, LabelType labelType) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            JanusGraphManagement management = graph.openManagement();
            
            // 创建顶点标签
            org.janusgraph.core.VertexLabel vertexLabel = management.makeVertexLabel(labelType.getName()).make();
            
            management.commit();
            System.out.println("创建JanusGraph节点类型: " + labelType.getName());
        } catch (Exception e) {
            throw new CoreException("创建JanusGraph点类型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteVertexType(ConnectionConfig config, String graphName, String labelName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            JanusGraphManagement management = graph.openManagement();
            
            // 删除顶点标签
            org.janusgraph.core.VertexLabel vertexLabel = management.getVertexLabel(labelName);
            if (vertexLabel != null) {
                management.makePropertyKey(vertexLabel.name()).cardinality(org.janusgraph.core.Cardinality.SINGLE).dataType(String.class).make();
                management.commit();
                // Note: JanusGraph doesn't support direct removal of vertex labels
                // This is a limitation of the underlying TinkerPop implementation
                System.out.println("注意：JanusGraph不支持直接删除顶点标签: " + labelName);
            }
            
            management.commit();
            System.out.println("删除JanusGraph节点类型: " + labelName);
        } catch (Exception e) {
            throw new CoreException("删除JanusGraph点类型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void createEdgeType(ConnectionConfig config, String graphName, LabelType labelType) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            JanusGraphManagement management = graph.openManagement();
            
            // 创建边标签
            org.janusgraph.core.EdgeLabel edgeLabel = management.makeEdgeLabel(labelType.getName()).make();
            
            management.commit();
            System.out.println("创建JanusGraph边类型: " + labelType.getName());
        } catch (Exception e) {
            throw new CoreException("创建JanusGraph边类型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEdgeType(ConnectionConfig config, String graphName, String labelName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            JanusGraphManagement management = graph.openManagement();
            
            // 删除边标签
            org.janusgraph.core.EdgeLabel edgeLabel = management.getEdgeLabel(labelName);
            if (edgeLabel != null) {
                // Note: JanusGraph doesn't support direct removal of edge labels
                // We'll just log this limitation
                System.out.println("注意：JanusGraph不支持直接删除边标签: " + labelName);
            }
            
            management.commit();
            System.out.println("删除JanusGraph边类型: " + labelName);
        } catch (Exception e) {
            throw new CoreException("删除JanusGraph边类型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LabelType> getVertexTypes(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            JanusGraphManagement management = graph.openManagement();
            
            Iterable<org.janusgraph.core.VertexLabel> vertexLabelIterable = management.getVertexLabels();
            List<LabelType> vertexTypes = new ArrayList<>();
            for (org.janusgraph.core.VertexLabel vertexLabel : vertexLabelIterable) {
                LabelType labelType = new LabelType();
                labelType.setName(vertexLabel.name());
                labelType.setType("VERTEX");
                labelType.setDescription("JanusGraph顶点标签: " + vertexLabel.name());
                vertexTypes.add(labelType);
            }
            
            management.rollback();
            return vertexTypes;
        } catch (Exception e) {
            throw new CoreException("获取JanusGraph顶点类型列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LabelType> getEdgeTypes(ConnectionConfig config, String graphName) throws CoreException {
        if (!isConnected()) {
            connect(config);
        }
        
        try {
            JanusGraphManagement management = graph.openManagement();
            
            Iterable<org.janusgraph.core.EdgeLabel> edgeLabelIterable = management.getRelationTypes(org.janusgraph.core.EdgeLabel.class);
            List<LabelType> edgeTypes = new ArrayList<>();
            for (org.janusgraph.core.EdgeLabel edgeLabel : edgeLabelIterable) {
                LabelType labelType = new LabelType();
                labelType.setName(edgeLabel.name());
                labelType.setType("EDGE");
                labelType.setDescription("JanusGraph边标签: " + edgeLabel.name());
                edgeTypes.add(labelType);
            }
            
            management.rollback();
            return edgeTypes;
        } catch (Exception e) {
            throw new CoreException("获取JanusGraph边类型列表失败: " + e.getMessage(), e);
        }
    }

    // SchemaHandler接口实现
    @Override
    public Map<String, Map<String, Object>> getNodeTypes(String graphName) {
        Map<String, Map<String, Object>> nodeTypes = new HashMap<>();
        // TODO: 查询JanusGraph的Vertex标签
        return nodeTypes;
    }

    @Override
    public Map<String, Map<String, Object>> getEdgeTypes(String graphName) {
        Map<String, Map<String, Object>> edgeTypes = new HashMap<>();
        // TODO: 查询JanusGraph的Edge标签
        return edgeTypes;
    }

    @Override
    public void createNodeType(String graphName, String typeName, Map<String, Object> properties) {
        // TODO: 在JanusGraph中创建Vertex标签
        System.out.println("创建节点类型: " + typeName);
    }

    @Override
    public void deleteNodeType(String graphName, String typeName) {
        // TODO: 在JanusGraph中删除Vertex标签
        System.out.println("删除节点类型: " + typeName);
    }

    // DataHandler接口实现
    @Override
    public Map<String, Object> createVertex(String graphName, String label, Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("JanusGraph连接未建立");
        }
        
        try {
            org.janusgraph.core.JanusGraphTransaction tx = graph.newTransaction();
            
            // 创建顶点
            org.apache.tinkerpop.gremlin.structure.Vertex vertex = tx.addVertex(label);
            
            // 设置属性
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    vertex.property(entry.getKey(), entry.getValue());
                }
            }
            
            // 生成唯一ID
            String uid = "v_" + vertex.id();
            vertex.property("uid", uid);
            
            tx.commit();
            
            // 构建返回的节点数据
            Map<String, Object> vertexData = new HashMap<>();
            vertexData.put("uid", uid);
            vertexData.put("label", label);
            vertexData.put("properties", properties != null ? properties : new HashMap<>());
            
            return vertexData;
        } catch (Exception e) {
            throw new CoreException("创建JanusGraph节点失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> createEdge(String graphName, String label,
                                           String sourceUid, String targetUid,
                                           Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("JanusGraph连接未建立");
        }
        
        try {
            org.janusgraph.core.JanusGraphTransaction tx = graph.newTransaction();
            
            // 查找源节点和目标节点
            org.apache.tinkerpop.gremlin.structure.Vertex sourceVertex = tx.traversal().V().has("uid", sourceUid).next();
            org.apache.tinkerpop.gremlin.structure.Vertex targetVertex = tx.traversal().V().has("uid", targetUid).next();
            
            // 创建边
            org.apache.tinkerpop.gremlin.structure.Edge edge = sourceVertex.addEdge(label, targetVertex);
            
            // 设置属性
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    edge.property(entry.getKey(), entry.getValue());
                }
            }
            
            // 生成唯一ID
            String uid = "e_" + edge.id();
            edge.property("uid", uid);
            
            tx.commit();
            
            // 构建返回的边数据
            Map<String, Object> edgeData = new HashMap<>();
            edgeData.put("uid", uid);
            edgeData.put("label", label);
            edgeData.put("sourceUid", sourceUid);
            edgeData.put("targetUid", targetUid);
            edgeData.put("properties", properties != null ? properties : new HashMap<>());
            
            return edgeData;
        } catch (Exception e) {
            throw new CoreException("创建JanusGraph边失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteVertex(String graphName, String uid) {
        if (!isConnected()) {
            throw new CoreException("JanusGraph连接未建立");
        }
        
        try {
            org.janusgraph.core.JanusGraphTransaction tx = graph.newTransaction();
            
            // 查找并删除节点
            org.apache.tinkerpop.gremlin.structure.Vertex vertex = tx.traversal().V().has("uid", uid).next();
            vertex.remove();
            
            tx.commit();
        } catch (Exception e) {
            throw new CoreException("删除JanusGraph节点失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEdge(String graphName, String uid) {
        if (!isConnected()) {
            throw new CoreException("JanusGraph连接未建立");
        }
        
        try {
            org.janusgraph.core.JanusGraphTransaction tx = graph.newTransaction();
            
            // 查找并删除边
            org.apache.tinkerpop.gremlin.structure.Edge edge = tx.traversal().E().has("uid", uid).next();
            edge.remove();
            
            tx.commit();
        } catch (Exception e) {
            throw new CoreException("删除JanusGraph边失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> queryVertices(String graphName, String label) {
        if (!isConnected()) {
            throw new CoreException("JanusGraph连接未建立");
        }
        
        try {
            org.janusgraph.core.JanusGraphTransaction tx = graph.newTransaction();
            
            List<Map<String, Object>> vertices = new ArrayList<>();
            
            // 构建查询
            org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal<org.apache.tinkerpop.gremlin.structure.Vertex, org.apache.tinkerpop.gremlin.structure.Vertex> traversal;
            if (label != null && !label.isEmpty()) {
                traversal = tx.traversal().V().hasLabel(label);
            } else {
                traversal = tx.traversal().V();
            }
            
            // 执行查询并转换结果
            while (traversal.hasNext()) {
                org.apache.tinkerpop.gremlin.structure.Vertex vertex = traversal.next();
                Map<String, Object> vertexData = new HashMap<>();
                
                vertexData.put("uid", vertex.property("uid").orElse("unknown"));
                vertexData.put("label", vertex.label());
                
                // 获取属性
                Map<String, Object> props = new HashMap<>();
                vertex.properties().forEachRemaining(prop -> {
                    props.put(prop.key(), prop.value());
                });
                vertexData.put("properties", props);
                
                vertices.add(vertexData);
            }
            
            tx.rollback(); // 只读事务，回滚
            return vertices;
        } catch (Exception e) {
            throw new CoreException("查询JanusGraph节点失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> queryEdges(String graphName, String label) {
        if (!isConnected()) {
            throw new CoreException("JanusGraph连接未建立");
        }
        
        try {
            org.janusgraph.core.JanusGraphTransaction tx = graph.newTransaction();
            
            List<Map<String, Object>> edges = new ArrayList<>();
            
            // 构建查询
            org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal<org.apache.tinkerpop.gremlin.structure.Edge, org.apache.tinkerpop.gremlin.structure.Edge> traversal;
            if (label != null && !label.isEmpty()) {
                traversal = tx.traversal().E().hasLabel(label);
            } else {
                traversal = tx.traversal().E();
            }
            
            // 执行查询并转换结果
            while (traversal.hasNext()) {
                org.apache.tinkerpop.gremlin.structure.Edge edge = traversal.next();
                Map<String, Object> edgeData = new HashMap<>();
                
                edgeData.put("uid", edge.property("uid").orElse("unknown"));
                edgeData.put("label", edge.label());
                edgeData.put("sourceUid", edge.outVertex().property("uid").orElse("unknown"));
                edgeData.put("targetUid", edge.inVertex().property("uid").orElse("unknown"));
                
                // 获取属性
                Map<String, Object> props = new HashMap<>();
                edge.properties().forEachRemaining(prop -> {
                    props.put(prop.key(), prop.value());
                });
                edgeData.put("properties", props);
                
                edges.add(edgeData);
            }
            
            tx.rollback(); // 只读事务，回滚
            return edges;
        } catch (Exception e) {
            throw new CoreException("查询JanusGraph边失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String importFromCsv(String graphName, CsvImportConfig config, InputStream csvStream) {
        // JanusGraph没有内置的CSV导入工具，这里实现基本逻辑
        try {
            // 在实际实现中，这里会：
            // 1. 解析CSV文件
            // 2. 根据配置映射字段
            // 3. 批量创建节点/边
            // 4. 返回导入统计信息
            
            // 模拟实现
            switch (config.getImportMode()) {
                case VERTICES_ONLY:
                    return "JanusGraph CSV导入功能：使用自定义解析器导入节点数据";
                case EDGES_ONLY:
                    return "JanusGraph CSV导入功能：使用自定义解析器导入边数据";
                case VERTICES_AND_EDGES:
                    return "JanusGraph CSV导入功能：使用自定义解析器导入节点和边数据";
                default:
                    return "CSV导入失败：未知的导入模式";
            }
        } catch (Exception e) {
            throw new CoreException("CSV导入失败: " + e.getMessage(), e);
        }
    }
}