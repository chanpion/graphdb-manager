package com.grapdb.adapter.neo4j;

import com.graphdb.adapter.neo4j.Neo4jAdapter;
import com.graphdb.core.model.ConnectionConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Neo4j 适配器测试类
 */
public class Neo4jTest {

    private Neo4jAdapter neo4jAdapter;
    private ConnectionConfig connectionConfig;

    @Before
    public void setUp() {
        neo4jAdapter = new Neo4jAdapter();

        // 配置 Neo4j 连接参数
        connectionConfig = new ConnectionConfig();
        connectionConfig.setHost("localhost");
        connectionConfig.setPort(7687); // Neo4j Bolt 协议默认端口
        connectionConfig.setUsername("neo4j");
        connectionConfig.setPassword("neo4j123"); // 请替换为实际密码
        connectionConfig.setDatabaseName("neo4j"); // 默认数据库名
        connectionConfig.setType("NEO4J");
        connectionConfig.setName("test-neo4j-connection");
    }

    @After
    public void tearDown() {
        // 测试结束后断开连接
        if (neo4jAdapter != null && neo4jAdapter.isConnected()) {
            neo4jAdapter.disconnect();
        }
    }

    /**
     * 测试获取 Neo4j 连接
     */
    @Test
    public void testGetConnection() {
        try {
            // 建立连接
            neo4jAdapter.connect(connectionConfig);

            // 验证连接是否成功建立
            assertTrue("Neo4j 连接应该成功建立", neo4jAdapter.isConnected());
            System.out.println("✓ 成功获取 Neo4j 连接：" + connectionConfig.getHost() + ":" + connectionConfig.getPort());
        } catch (Exception e) {
            fail("获取 Neo4j 连接失败：" + e.getMessage());
        }
    }

    /**
     * 测试查询当前连接的 Neo4j 图数据库名
     */
    @Test
    public void testQueryGraphName() {
        try {
            // 先建立连接
            neo4jAdapter.connect(connectionConfig);

            // 获取图列表（Neo4j 中返回数据库名称列表）
            var graphs = neo4jAdapter.getGraphs(connectionConfig);

            // 验证返回结果
            assertNotNull("图列表不应为空", graphs);
            assertFalse("图列表不应该为空", graphs.isEmpty());

            // 打印数据库名称
            System.out.println("✓ 当前连接的 Neo4j 图数据库名：" + graphs.get(0));
            assertEquals("默认数据库名应为 neo4j", "neo4j", graphs.get(0));

        } catch (Exception e) {
            fail("查询 Neo4j 图数据库名失败：" + e.getMessage());
        }
    }

    /**
     * 测试获取图 Schema（包含标签和关系类型）
     */
    @Test
    public void testGetGraphSchema() {
        try {
            // 先建立连接
            neo4jAdapter.connect(connectionConfig);

            // 获取图 Schema
            var schema = neo4jAdapter.getGraphSchema(connectionConfig, "neo4j");

            // 验证返回结果
            assertNotNull("图 Schema 不应为空", schema);
            assertEquals("数据库类型应为 NEO4J", "NEO4J", schema.getDatabaseType());
            assertEquals("图名称应为 neo4j", "neo4j", schema.getGraphName());

            // 打印 Schema 信息
            System.out.println("✓ 图数据库名：" + schema.getGraphName());
            System.out.println("✓ 节点标签数量：" + (schema.getVertexLabels() != null ? schema.getVertexLabels().size() : 0));
            System.out.println("✓ 边类型数量：" + (schema.getEdgeLabels() != null ? schema.getEdgeLabels().size() : 0));
            schema.getVertexLabels().forEach(label -> {
                System.out.println("  - 节点标签：" + label.getName());
                System.out.println("    - 属性数量：" + (label.getProperties() != null ? label.getProperties().size() : 0));
            });
            schema.getEdgeLabels().forEach(label -> {
                System.out.println("  - 边类型：" + label.getName());
                System.out.println("    - 属性数量：" + (label.getProperties() != null ? label.getProperties().size() : 0));
            });

        } catch (Exception e) {
            fail("获取图 Schema 失败：" + e.getMessage());
        }
    }
}
