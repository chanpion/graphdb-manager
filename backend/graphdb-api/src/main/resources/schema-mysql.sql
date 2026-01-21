-- MySQL Schema for GraphDB Manager
-- Generated on 2026-01-21
-- This script initializes the database schema for GraphDB Manager in MySQL.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Drop tables in reverse dependency order (if they exist)
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `import_task`;
DROP TABLE IF EXISTS `query_script`;
DROP TABLE IF EXISTS `query_history`;
DROP TABLE IF EXISTS `graph_instance`;
DROP TABLE IF EXISTS `connection_config`;

-- 连接配置表
CREATE TABLE IF NOT EXISTS `connection_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(100) NOT NULL COMMENT '连接名称',
    `database_type` varchar(20) NOT NULL COMMENT '数据库类型：NEO4J/NEBULA/JANUS',
    `host` varchar(100) NOT NULL COMMENT '主机地址',
    `port` int NOT NULL COMMENT '端口号',
    `username` varchar(100) DEFAULT NULL COMMENT '用户名',
    `password_encrypted` varchar(500) DEFAULT NULL COMMENT '加密后的密码',
    `database_name` varchar(100) DEFAULT NULL COMMENT '数据库名/图空间',
    `storage_type` varchar(20) DEFAULT NULL COMMENT '存储类型（仅JANUS）：HBASE/CASSANDRA',
    `storage_config` text DEFAULT NULL COMMENT '存储配置参数（仅JANUS）',
    `extra_params` text DEFAULT NULL COMMENT '额外连接参数（JSON格式）',
    `status` int DEFAULT 0 COMMENT '连接状态：0-未测试，1-正常，2-异常',
    `description` varchar(500) DEFAULT NULL COMMENT '连接描述',
    `priority` int DEFAULT 5 COMMENT '优先级：1-10，数字越小优先级越高',
    `created_by` varchar(100) DEFAULT 'system' COMMENT '创建人',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` int DEFAULT 0 COMMENT '逻辑删除标记（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_database_type` (`database_type`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='连接配置表';

-- 图实例表
CREATE TABLE IF NOT EXISTS `graph_instance` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `connection_id` bigint NOT NULL COMMENT '关联连接ID',
    `graph_name` varchar(100) NOT NULL COMMENT '图名称',
    `database_type` varchar(20) NOT NULL COMMENT '数据库类型',
    `vertex_count` bigint DEFAULT 0 COMMENT '节点数量',
    `edge_count` bigint DEFAULT 0 COMMENT '边数量',
    `status` varchar(20) DEFAULT 'NORMAL' COMMENT '图状态：NORMAL/ARCHIVED',
    `description` varchar(500) DEFAULT NULL COMMENT '图描述',
    `schema_version` varchar(50) DEFAULT NULL COMMENT 'Schema版本号',
    `cache_time` datetime DEFAULT NULL COMMENT 'Schema缓存时间',
    `is_default` int DEFAULT 0 COMMENT '是否默认图',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_accessed_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后访问时间',
    `deleted` int DEFAULT 0 COMMENT '逻辑删除标记（0-未删除，1-已删除）',
    `source_type` varchar(20) DEFAULT 'PLATFORM' COMMENT '图来源：PLATFORM(平台创建)/EXISTING(图数据库已有)',
    PRIMARY KEY (`id`),
    KEY `idx_connection_id` (`connection_id`),
    KEY `idx_database_type` (`database_type`),
    KEY `idx_status` (`status`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_last_accessed_at` (`last_accessed_at`),
    CONSTRAINT `fk_graph_instance_connection` FOREIGN KEY (`connection_id`) REFERENCES `connection_config` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图实例表';

-- 查询历史表
CREATE TABLE IF NOT EXISTS `query_history` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `connection_id` bigint NOT NULL COMMENT '关联连接ID',
    `graph_instance_id` bigint NOT NULL COMMENT '关联图实例ID',
    `query` text NOT NULL COMMENT '查询语句',
    `execution_time_ms` bigint DEFAULT NULL COMMENT '执行时间（毫秒）',
    `result_count` bigint DEFAULT NULL COMMENT '结果数量',
    `status` varchar(20) DEFAULT 'SUCCESS' COMMENT '状态：SUCCESS/FAILED/TIMEOUT',
    `error_message` text DEFAULT NULL COMMENT '错误信息',
    `executed_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    `created_by` varchar(100) DEFAULT 'system' COMMENT '执行人',
    `deleted` int DEFAULT 0 COMMENT '逻辑删除标记（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_connection_id` (`connection_id`),
    KEY `idx_graph_instance_id` (`graph_instance_id`),
    KEY `idx_executed_at` (`executed_at`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_query_history_connection` FOREIGN KEY (`connection_id`) REFERENCES `connection_config` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_query_history_graph_instance` FOREIGN KEY (`graph_instance_id`) REFERENCES `graph_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='查询历史表';

-- 查询脚本表
CREATE TABLE IF NOT EXISTS `query_script` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `connection_id` bigint NOT NULL COMMENT '关联连接ID',
    `graph_instance_id` bigint NOT NULL COMMENT '关联图实例ID',
    `name` varchar(100) NOT NULL COMMENT '脚本名称',
    `description` text DEFAULT NULL COMMENT '脚本描述',
    `queries` text NOT NULL COMMENT '查询语句列表（JSON数组格式）',
    `created_by` varchar(100) DEFAULT 'system' COMMENT '创建人',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` int DEFAULT 0 COMMENT '逻辑删除标记（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_connection_id` (`connection_id`),
    KEY `idx_graph_instance_id` (`graph_instance_id`),
    KEY `idx_name` (`name`),
    CONSTRAINT `fk_query_script_connection` FOREIGN KEY (`connection_id`) REFERENCES `connection_config` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_query_script_graph_instance` FOREIGN KEY (`graph_instance_id`) REFERENCES `graph_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='查询脚本表';

-- 导入任务表
CREATE TABLE IF NOT EXISTS `import_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `connection_id` bigint NOT NULL COMMENT '关联连接ID',
    `graph_instance_id` bigint NOT NULL COMMENT '关联图实例ID',
    `task_name` varchar(100) NOT NULL COMMENT '任务名称',
    `file_name` varchar(255) NOT NULL COMMENT '文件名',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
    `total_records` bigint DEFAULT NULL COMMENT '总记录数',
    `processed_records` bigint DEFAULT 0 COMMENT '已处理记录数',
    `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/SUCCESS/FAILED',
    `error_message` text DEFAULT NULL COMMENT '错误信息',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `created_by` varchar(100) DEFAULT 'system' COMMENT '创建人',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` int DEFAULT 0 COMMENT '逻辑删除标记（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_connection_id` (`connection_id`),
    KEY `idx_graph_instance_id` (`graph_instance_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_import_task_connection` FOREIGN KEY (`connection_id`) REFERENCES `connection_config` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_import_task_graph_instance` FOREIGN KEY (`graph_instance_id`) REFERENCES `graph_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导入任务表';

-- 用户表（用于系统认证）
CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` varchar(100) NOT NULL UNIQUE COMMENT '用户名',
    `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
    `display_name` varchar(100) DEFAULT NULL COMMENT '显示名称',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `role` varchar(20) DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
    `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
    `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` int DEFAULT 0 COMMENT '逻辑删除标记（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Reset foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- End of schema script