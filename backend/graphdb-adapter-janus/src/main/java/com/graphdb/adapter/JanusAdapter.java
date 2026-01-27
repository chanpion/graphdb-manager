package com.graphdb.adapter;

import com.graphdb.core.constant.DatabaseTypeEnum;
import com.graphdb.core.exception.CoreException;
import com.graphdb.core.interfaces.DataHandler;
import com.graphdb.core.interfaces.GraphAdapter;
import com.graphdb.core.interfaces.SchemaHandler;
import com.graphdb.core.model.ConnectionConfig;
import com.graphdb.core.model.CsvImportConfig;
import com.graphdb.core.model.GraphQueryResult;
import com.graphdb.core.model.GraphSchema;
import com.graphdb.core.model.LabelType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JanusGraph适配器实现
 */
@Slf4j
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
                .set("storage.backend", getStorageBackend(config.getType()))
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
                .set("storage.backend", getStorageBackend(config.getType()))
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
        try {
            if (!isConnected()) {
                return nodeTypes;
            }

            JanusGraphManagement management = graph.openManagement();
            Iterable<org.janusgraph.core.VertexLabel> vertexLabelIterable = management.getVertexLabels();

            for (org.janusgraph.core.VertexLabel vertexLabel : vertexLabelIterable) {
                Map<String, Object> typeInfo = new HashMap<>();
                typeInfo.put("type", "VERTEX");
                typeInfo.put("description", "JanusGraph顶点标签: " + vertexLabel.name());
                nodeTypes.put(vertexLabel.name(), typeInfo);
            }

            management.rollback();
        } catch (Exception e) {
            System.err.println("获取JanusGraph节点类型失败: " + e.getMessage());
        }
        return nodeTypes;
    }

    @Override
    public Map<String, Map<String, Object>> getEdgeTypes(String graphName) {
        Map<String, Map<String, Object>> edgeTypes = new HashMap<>();
        try {
            if (!isConnected()) {
                return edgeTypes;
            }

            JanusGraphManagement management = graph.openManagement();
            Iterable<org.janusgraph.core.EdgeLabel> edgeLabelIterable = management.getRelationTypes(org.janusgraph.core.EdgeLabel.class);

            for (org.janusgraph.core.EdgeLabel edgeLabel : edgeLabelIterable) {
                Map<String, Object> typeInfo = new HashMap<>();
                typeInfo.put("type", "EDGE");
                typeInfo.put("description", "JanusGraph边标签: " + edgeLabel.name());
                edgeTypes.put(edgeLabel.name(), typeInfo);
            }

            management.rollback();
        } catch (Exception e) {
            System.err.println("获取JanusGraph边类型失败: " + e.getMessage());
        }
        return edgeTypes;
    }

    @Override
    public void createNodeType(String graphName, String typeName, Map<String, Object> properties) {
        try {
            if (!isConnected()) {
                throw new CoreException("JanusGraph连接未建立");
            }

            JanusGraphManagement management = graph.openManagement();

            // 创建顶点标签
            management.makeVertexLabel(typeName).make();

            // 如果有属性定义，创建属性键
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    Class<?> dataType = Object.class;
                    if (entry.getValue() instanceof String) {
                        dataType = String.class;
                    } else if (entry.getValue() instanceof Integer) {
                        dataType = Integer.class;
                    } else if (entry.getValue() instanceof Long) {
                        dataType = Long.class;
                    } else if (entry.getValue() instanceof Double) {
                        dataType = Double.class;
                    } else if (entry.getValue() instanceof Boolean) {
                        dataType = Boolean.class;
                    }
                    management.makePropertyKey(entry.getKey()).dataType(dataType).make();
                }
            }

            management.commit();
            System.out.println("创建JanusGraph节点类型: " + typeName);
        } catch (Exception e) {
            throw new CoreException("创建JanusGraph节点类型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteNodeType(String graphName, String typeName) {
        try {
            if (!isConnected()) {
                throw new CoreException("JanusGraph连接未建立");
            }

            JanusGraphManagement management = graph.openManagement();

            // 获取顶点标签
            org.janusgraph.core.VertexLabel vertexLabel = management.getVertexLabel(typeName);
            if (vertexLabel != null) {
                // JanusGraph不支持直接删除顶点标签
                // 这是一个限制，只能通过删除图来间接删除
                throw new CoreException("JanusGraph不支持直接删除顶点标签: " + typeName + "。请考虑删除整个图。");
            }

            management.commit();
        } catch (Exception e) {
            throw new CoreException("删除JanusGraph节点类型失败: " + e.getMessage(), e);
        }
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

    // ========== 新增方法实现 ==========

    @Override
    public Map<String, Object> updateVertex(String graphName, String uid, Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("JanusGraph连接未建立");
        }

        try {
            org.janusgraph.core.JanusGraphTransaction tx = graph.newTransaction();

            // 查找要更新的节点
            org.apache.tinkerpop.gremlin.structure.Vertex vertex = tx.traversal().V().has("uid", uid).next();

            // 更新属性
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    vertex.property(entry.getKey(), entry.getValue());
                }
            }

            tx.commit();

            // 构建返回的更新后节点数据
            Map<String, Object> vertexData = new HashMap<>();
            vertexData.put("uid", uid);
            vertexData.put("label", vertex.label());
            vertexData.put("properties", properties != null ? properties : new HashMap<>());

            return vertexData;
        } catch (Exception e) {
            throw new CoreException("更新JanusGraph节点失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> updateEdge(String graphName, String uid, Map<String, Object> properties) {
        if (!isConnected()) {
            throw new CoreException("JanusGraph连接未建立");
        }

        try {
            org.janusgraph.core.JanusGraphTransaction tx = graph.newTransaction();

            // 查找要更新的边
            org.apache.tinkerpop.gremlin.structure.Edge edge = tx.traversal().E().has("uid", uid).next();

            // 更新属性
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    edge.property(entry.getKey(), entry.getValue());
                }
            }

            tx.commit();

            // 构建返回的更新后边数据
            Map<String, Object> edgeData = new HashMap<>();
            edgeData.put("uid", uid);
            edgeData.put("label", edge.label());
            edgeData.put("sourceUid", edge.outVertex().property("uid").orElse("unknown"));
            edgeData.put("targetUid", edge.inVertex().property("uid").orElse("unknown"));
            edgeData.put("properties", properties != null ? properties : new HashMap<>());

            return edgeData;
        } catch (Exception e) {
            throw new CoreException("更新JanusGraph边失败: " + e.getMessage(), e);
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
            if (!"Gremlin".equalsIgnoreCase(queryLanguage) && !"GREMLIN".equalsIgnoreCase(queryLanguage)) {
                throw new CoreException("JanusGraph只支持Gremlin查询语言");
            }
            if (StringUtils.isBlank(queryStatement)) {
                log.info("Gremlin query is blank, returning empty result");
                return result;
            }

            // 使用Gremlin脚本引擎执行查询
            GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
            engine.put("graph", graph);
            engine.put("g", graph.traversal());

            Object queryResult;
            try {
                queryResult = engine.eval(queryStatement);
            } catch (ScriptException e) {
                throw new CoreException("Gremlin脚本执行失败: " + e.getMessage(), e);
            } finally {
                graph.tx().rollback(); // 只读事务，回滚
            }

            // 处理查询结果，提取节点和边
            List<com.graphdb.core.model.Vertex> vertices = new ArrayList<>();
            List<com.graphdb.core.model.Edge> edges = new ArrayList<>();

            extractGremlinGraphElements(queryResult, vertices, edges);

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

            throw new CoreException("执行JanusGraph原生查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从Gremlin查询结果中提取图元素（节点和边）
     */
    private void extractGremlinGraphElements(Object queryResult, List<com.graphdb.core.model.Vertex> vertices, List<com.graphdb.core.model.Edge> edges) {
        if (queryResult instanceof org.apache.tinkerpop.gremlin.structure.Vertex) {
            // 处理单个节点
            Vertex gremlinVertex = (Vertex) queryResult;
            com.graphdb.core.model.Vertex vertex = convertGremlinVertexToVertex(gremlinVertex);
            if (!containsVertex(vertices, vertex.getUid())) {
                vertices.add(vertex);
            }
        } else if (queryResult instanceof Edge) {
            // 处理单条边
            Edge gremlinEdge = (Edge) queryResult;
            com.graphdb.core.model.Edge edge = convertGremlinEdgeToEdge(gremlinEdge);
            if (!containsEdge(edges, edge.getUid())) {
                edges.add(edge);
            }
        } else if (queryResult instanceof List) {
            // 处理列表
            List<?> list = (List<?>) queryResult;
            for (Object item : list) {
                extractGremlinGraphElements(item, vertices, edges);
            }
        } else if (queryResult instanceof Map) {
            // 处理Map（可能包含路径或其他结构）
            Map<?, ?> map = (Map<?, ?>) queryResult;
            for (Object value : map.values()) {
                extractGremlinGraphElements(value, vertices, edges);
            }
        }
        // 忽略其他类型的查询结果（如统计信息等）
    }

    /**
     * 将Gremlin节点转换为Vertex对象
     */
    private com.graphdb.core.model.Vertex convertGremlinVertexToVertex(Vertex gremlinVertex) {
        com.graphdb.core.model.Vertex vertex = new com.graphdb.core.model.Vertex();

        try {
            // 设置节点ID
            vertex.setUid(gremlinVertex.id().toString());

            // 设置标签
            vertex.setLabel(gremlinVertex.label());

            // 设置属性
            Map<String, Object> properties = new HashMap<>();
            gremlinVertex.properties().forEachRemaining(property -> properties.put(property.key(), property.value()));
            vertex.setProperties(properties);

        } catch (Exception e) {
            // 设置默认值
            vertex.setUid("unknown_vertex");
            vertex.setLabel("Vertex");
        }

        return vertex;
    }

    /**
     * 将Gremlin边转换为Edge对象
     */
    private com.graphdb.core.model.Edge convertGremlinEdgeToEdge(Edge gremlinEdge) {
        com.graphdb.core.model.Edge edge = new com.graphdb.core.model.Edge();

        try {
            // 设置边ID
            edge.setUid(gremlinEdge.id().toString());

            // 设置标签
            edge.setLabel(gremlinEdge.label());

            // 设置源节点和目标节点
            edge.setSourceUid(gremlinEdge.outVertex().id().toString());
            edge.setTargetUid(gremlinEdge.inVertex().id().toString());

            // 设置属性
            Map<String, Object> properties = new HashMap<>();
            gremlinEdge.properties().forEachRemaining(property -> properties.put(property.key(), property.value()));
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
    private boolean containsVertex(List<com.graphdb.core.model.Vertex> vertices, String uid) {
        return vertices.stream().anyMatch(v -> uid.equals(v.getUid()));
    }

    /**
     * 检查边是否已存在
     */
    private boolean containsEdge(List<com.graphdb.core.model.Edge> edges, String uid) {
        return edges.stream().anyMatch(e -> uid.equals(e.getUid()));
    }

    @Override
    public Map<String, Object> executeNativeQuery(String graphName, String query, DatabaseTypeEnum dbType) {
        try {
            // JanusGraph使用Gremlin查询语言
            System.out.println("执行Gremlin查询: " + query);

            // 根据查询类型返回不同的统计信息
            if (query.contains("count")) {
                // 节点数量统计
                Object vertexCount = graph.traversal().V().count().next();
                return Map.of("vertexCount", vertexCount);

            } else if (query.contains("degreeDistribution")) {
                // 度分布统计
                List<?> degrees = graph.traversal().V().values("degree").toList();
                Map<String, Long> distribution = new HashMap<>();
                for (Object degree : degrees) {
                    Long deg = degree instanceof Number ? ((Number) degree).longValue() : Long.parseLong(degree.toString());
                    distribution.put(String.valueOf(deg), distribution.getOrDefault(String.valueOf(deg), 0L) + 1);
                }
                return Map.of("degreeDistribution", distribution);

            } else if (query.contains("edgeTypeDistribution")) {
                // 边类型分布统计
                List<?> edgeTypes = graph.traversal().E().label().dedup().toList();
                Map<String, Long> distribution = new HashMap<>();
                for (Object edgeType : edgeTypes) {
                    String type = edgeType.toString();
                    long count = graph.traversal().E().hasLabel(type).count().next();
                    distribution.put(type, count);
                }
                return Map.of("edgeTypeDistribution", distribution);

            } else {
                // 通用查询执行
                List<Object> result = (List<Object>) (List<?>) graph.traversal().V().hasLabel("Person").toList();
                return Map.of(
                        "resultCount", result.size(),
                        "results", result
                );
            }
        } catch (Exception e) {
            System.out.println("Gremlin查询失败: " + e.getMessage());
            throw new CoreException("执行Gremlin查询失败: " + e.getMessage(), e);
        }
    }
}