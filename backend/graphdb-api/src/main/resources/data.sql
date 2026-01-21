-- 图数据库管理系统初始化数据

-- 插入默认连接配置数据
INSERT IGNORE INTO `connection_config` (`id`, `name`, `database_type`, `host`, `port`, `username`, `description`, `status`, `priority`, `created_by`) VALUES
(1, '本地Neo4j测试', 'NEO4J', 'localhost', 7474, 'neo4j', '本地Neo4j数据库测试连接', 1, 1, 'system'),
(2, '本地Nebula测试', 'NEBULA', 'localhost', 9669, 'root', '本地NebulaGraph数据库测试连接', 0, 2, 'system'),
(3, '本地Janus测试', 'JANUS', 'localhost', 8182, '', '本地JanusGraph数据库测试连接', 0, 3, 'system');

-- 插入默认图实例数据
INSERT IGNORE INTO `graph_instance` (`id`, `connection_id`, `graph_name`, `database_type`, `vertex_count`, `edge_count`, `description`, `is_default`, `source_type`) VALUES
(1, 1, '默认图', 'NEO4J', 0, 0, 'Neo4j默认图实例', 1, 'PLATFORM'),
(2, 2, '默认图空间', 'NEBULA', 0, 0, 'NebulaGraph默认图空间', 0, 'PLATFORM'),
(3, 3, '默认图', 'JANUS', 0, 0, 'JanusGraph默认图实例', 0, 'PLATFORM');

-- 插入查询历史示例数据
INSERT IGNORE INTO `query_history` (`id`, `connection_id`, `graph_instance_id`, `query`, `execution_time_ms`, `result_count`, `status`, `executed_at`, `created_by`) VALUES
(1, 1, 1, 'MATCH (p:Person) RETURN p LIMIT 10', 234, 10, 'SUCCESS', DATEADD('DAY', -1, NOW()), 'admin'),
(2, 1, 1, 'MATCH (p:Person)-[r:KNOWS]->(f:Person) WHERE p.name = "张三" RETURN p, r, f', 456, 85, 'SUCCESS', DATEADD('HOUR', -2, NOW()), 'admin'),
(3, 2, 2, 'USE graph_space; MATCH (v:Person) RETURN v LIMIT 5', 123, 5, 'SUCCESS', DATEADD('HOUR', -5, NOW()), 'user'),
(4, 3, 3, 'g.V().hasLabel("Person").limit(10)', 345, 10, 'SUCCESS', DATEADD('HOUR', -1, NOW()), 'admin'),
(5, 1, 1, 'MATCH (p:Person) WHERE p.age > 30 RETURN count(p)', 89, 1, 'FAILED', DATEADD('MINUTE', -30, NOW()), 'user');

-- 插入查询脚本示例数据
INSERT IGNORE INTO `query_script` (`id`, `connection_id`, `graph_instance_id`, `name`, `description`, `queries`, `created_by`) VALUES
(1, 1, 1, '社交网络分析', '分析社交网络中的关键人物和关系', '["MATCH (p:Person) RETURN p LIMIT 100", "MATCH (p:Person)-[r:KNOWS]->(f:Person) RETURN p, r, f LIMIT 50", "MATCH (p:Person) RETURN p.name, size((p)-[:KNOWS]->()) as friendCount ORDER BY friendCount DESC LIMIT 10"]', 'admin'),
(2, 2, 2, 'Nebula基础查询', 'NebulaGraph基础查询模板', '["USE graph_space; MATCH (v:Person) RETURN v LIMIT 10", "USE graph_space; MATCH ()-[e:KNOWS]->() RETURN e LIMIT 20"]', 'user'),
(3, 3, 3, 'Janus图遍历', 'JanusGraph Gremlin遍历示例', '["g.V().hasLabel(\"Person\").limit(10)", "g.V().has(\"age\", gt(30)).limit(20)", "g.E().hasLabel(\"KNOWS\").limit(15)"]', 'admin');

-- 插入导入任务示例数据
INSERT IGNORE INTO `import_task` (`id`, `connection_id`, `graph_instance_id`, `task_name`, `file_name`, `file_size`, `total_records`, `processed_records`, `status`, `start_time`, `end_time`, `created_by`) VALUES
(1, 1, 1, '导入用户数据', 'users.csv', 102400, 10000, 10000, 'SUCCESS', DATEADD('DAY', -3, NOW()), DATEADD('MINUTE', 30, DATEADD('DAY', -3, NOW())), 'admin'),
(2, 2, 2, '导入社交关系', 'relationships.csv', 204800, 50000, 25000, 'PROCESSING', DATEADD('HOUR', -1, NOW()), NULL, 'user'),
(3, 3, 3, '导入产品数据', 'products.csv', 51200, 5000, 0, 'PENDING', NULL, NULL, 'admin');

-- 插入默认用户数据
-- 默认管理员用户：admin / admin123
INSERT IGNORE INTO `user` (`id`, `username`, `password_hash`, `display_name`, `email`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$YourEncryptedPasswordHashHere', '系统管理员', 'admin@example.com', 'ADMIN', 'ACTIVE'),
(2, 'user', '$2a$10$YourEncryptedPasswordHashHere', '普通用户', 'user@example.com', 'USER', 'ACTIVE');