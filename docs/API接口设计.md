# 图数据库管理系统 - API接口设计文档

## 1. API设计概述

### 1.1 设计原则

**RESTful风格**：
- 使用HTTP方法语义：GET查询、POST创建、PUT更新、DELETE删除
- 资源命名使用复数形式：/api/connections, /api/graphs, /api/vertices
- 使用名词而非动词：使用 /graphs 而非 /getGraphs

**统一响应格式**：
- 成功响应：HTTP 200 OK，包含data和message字段
- 错误响应：4xx客户端错误，5xx服务端错误
- 分页响应：包含total、pageNum、pageSize、list字段
- 列表响应：包含total、list字段

**版本控制**：
- API版本号：/api/v1/
- 兼容性策略：新版本保持向后兼容至少6个月

### 1.2 统一响应格式

**成功响应**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 具体业务数据
  },
  "timestamp": 1704748800000,
  "traceId": "req_123456789"
}
```

**分页响应**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 1000,
    "pageNum": 1,
    "pageSize": 20,
    "list": [...]
  },
  "timestamp": 1704748800000
}
```

**错误响应**：
```json
{
  "code": 40001,
  "message": "连接名称已存在",
  "data": null,
  "timestamp": 1704748800000,
  "traceId": "req_123456789"
}
```

### 1.3 HTTP状态码

| 状态码 | 说明 | 使用场景 |
|-------|------|---------|
| 200 OK | 请求成功 |
| 201 Created | 资源创建成功 |
| 204 No Content | 删除成功 |
| 400 Bad Request | 请求参数错误 |
| 401 Unauthorized | 未认证或Token过期 |
| 403 Forbidden | 无权限访问 |
| 404 Not Found | 资源不存在 |
| 409 Conflict | 资源冲突（如重复） |
| 500 Internal Server Error | 服务器内部错误 |

### 1.4 错误码定义

#### 连接管理错误码

| 错误码 | 说明 | HTTP状态码 |
|-------|------|----------|
| 40001 | 连接名称已存在 | 409 |
| 40002 | 连接参数无效 | 400 |
| 40003 | 连接测试失败 | 400 |
| 40004 | 连接不存在 | 404 |
| 40005 | 连接已禁用 | 400 |

#### 图管理错误码

| 错误码 | 说明 | HTTP状态码 |
|-------|------|----------|
| 41001 | 图不存在 | 404 |
| 41002 | 图名称已存在 | 409 |
| 41003 | 创建图失败 | 500 |
| 41004 | 删除图失败 | 500 |
| 41005 | 图不可用 | 503 |

#### 数据操作错误码

| 错误码 | 说明 | HTTP状态码 |
|-------|------|----------|
| 42001 | 节点UID不存在 | 404 |
| 42002 | 边UID不存在 | 404 |
| 42003 | UID重复 | 409 |
| 42004 | 节点类型不存在 | 400 |
| 42005 | 边类型不存在 | 400 |
| 42006 | 引用完整性违规 | 400 |
| 42007 | 数据验证失败 | 400 |
| 42008 | 批量操作超出限制 | 400 |

#### Schema管理错误码

| 错误码 | 说明 | HTTP状态码 |
|-------|------|----------|
| 43001 | 节点类型已存在 | 409 |
| 43002 | 边类型已存在 | 409 |
| 43003 | 属性定义无效 | 400 |
| 43004 | 索引创建失败 | 500 |

#### 导入导出错误码

| 错误码 | 说明 | HTTP状态码 |
|-------|------|----------|
| 44001 | 文件格式不支持 | 400 |
| 44002 | 文件解析失败 | 400 |
| 44003 | 字段映射错误 | 400 |
| 44004 | 导入部分失败 | 207 |

## 2. 连接管理API

### 2.1 创建连接

**接口地址**：`POST /api/v1/connections`

**请求参数**：
```json
{
  "name": "生产环境Neo4j",
  "databaseType": "NEO4J",
  "host": "192.168.1.100",
  "port": 7687,
  "username": "neo4j",
  "password": "password123",
  "databaseName": "neo4j",
  "storageType": null,
  "storageConfig": null,
  "extraParams": {
    "maxTransactionRetryTime": 30
  },
  "description": "生产环境图数据库"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "连接创建成功",
  "data": {
    "id": "conn_001",
    "name": "生产环境Neo4j",
    "databaseType": "NEO4J",
    "host": "192.168.1.100",
    "port": 7687,
    "status": 1,
    "createdAt": 1704748800000
  },
  "timestamp": 1704748800000
}
```

### 2.2 编辑连接

**接口地址**：`PUT /api/v1/connections/{id}`

**请求参数**：
```json
{
  "name": "生产环境Neo4j",
  "host": "192.168.1.101",
  "port": 7687,
  "username": "neo4j",
  "password": "newpassword123",
  "databaseName": "neo4j",
  "description": "更新后的描述"
}
```

### 2.3 删除连接

**接口地址**：`DELETE /api/v1/connections/{id}`

**请求参数**：路径参数 `id`

**响应示例**：
```json
{
  "code": 200,
  "message": "连接删除成功",
  "data": null,
  "timestamp": 1704748800000
}
```

### 2.4 测试连接

**接口地址**：`POST /api/v1/connections/test`

**请求参数**：
```json
{
  "databaseType": "NEO4J",
  "host": "192.168.1.100",
  "port": 7687,
  "username": "neo4j",
  "password": "password123",
  "databaseName": "neo4j",
  "storageType": null,
  "storageConfig": null
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "连接测试成功",
  "data": {
    "success": true,
    "version": "4.4.11",
    "graphCount": 1,
    "connectionTime": 234
  },
  "timestamp": 1704748800000
}
```

### 2.5 查询连接列表

**接口地址**：`GET /api/v1/connections`

**查询参数**：
- `page`: 页码（默认1）
- `pageSize`: 每页大小（默认20）
- `databaseType`: 数据库类型筛选（可选）
- `status`: 状态筛选（可选）
- `keyword`: 关键词搜索（可选）

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 5,
    "pageNum": 1,
    "pageSize": 20,
    "list": [
      {
        "id": "conn_001",
        "name": "生产环境Neo4j",
        "databaseType": "NEO4J",
        "host": "192.168.1.100",
        "port": 7687,
        "status": 1,
        "description": "生产环境图数据库",
        "createdAt": 1704748800000
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 2.6 获取连接详情

**接口地址**：`GET /api/v1/connections/{id}`

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": "conn_001",
    "name": "生产环境Neo4j",
    "databaseType": "NEO4J",
    "host": "192.168.1.100",
    "port": 7687,
    "username": "neo4j",
    "databaseName": "neo4j",
    "storageType": null,
    "extraParams": {
      "maxTransactionRetryTime": 30
    },
    "description": "生产环境图数据库",
    "status": 1,
    "priority": 5,
    "createdBy": "admin",
    "createdAt": 1704748800000,
    "updatedAt": 1704748801000,
    "lastTestedAt": 1704748820000
  },
  "timestamp": 1704748800000
}
```

## 3. 图管理API

### 3.1 获取图列表

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs`

**查询参数**：
- `page`: 页码（默认1）
- `pageSize`: 每页大小（默认20）
- `status`: 状态筛选（可选）

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 10,
    "pageNum": 1,
    "pageSize": 20,
    "list": [
      {
        "graphName": "neo4j",
        "databaseType": "NEO4J",
        "vertexCount": 15234,
        "edgeCount": 45678,
        "status": "NORMAL",
        "description": "社交网络数据",
        "createdAt": 1704748800000,
        "updatedAt": 1704748820000
      },
      {
        "graphName": "space1",
        "databaseType": "NEBULA",
        "vertexCount": 8923,
        "edgeCount": 23456,
        "status": "NORMAL",
        "description": "用户关系图",
        "createdAt": 1704748700000,
        "updatedAt": 1704748850000
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 3.2 获取图详情

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}`

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "graphName": "neo4j",
    "databaseType": "NEO4J",
    "vertexCount": 15234,
    "edgeCount": 45678,
    "status": "NORMAL",
    "description": "社交网络数据",
    "schemaVersion": "v1.2",
    "createdAt": 1704748800000,
    "updatedAt": 1704748820000
  },
  "timestamp": 1704748800000
}
```

### 3.3 创建图

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs`

**请求参数**：
```json
{
  "graphName": "new_space",
  "description": "新的用户关系空间",
  "vertexCount": 0,
  "edgeCount": 0
}
```

**响应示例**：
```json
{
  "code": 201,
  "message": "图创建成功",
  "data": {
    "graphName": "new_space",
    "status": "NORMAL",
    "createdAt": 1704748800000
  },
  "timestamp": 1704748800000
}
```

### 3.4 删除图

**接口地址**：`DELETE /api/v1/connections/{connectionId}/graphs/{graphName}`

**响应示例**：
```json
{
  "code": 200,
  "message": "图删除成功",
  "data": {
    "deletedVertexCount": 8923,
    "deletedEdgeCount": 23456
  },
  "timestamp": 1704748800000
}
```

## 4. Schema管理API

### 4.1 获取节点类型列表

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/node-types`

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 5,
    "list": [
      {
        "name": "Person",
        "description": "人员节点",
        "propertiesCount": 8,
        "properties": [
          {
            "name": "name",
            "type": "STRING",
            "required": true,
            "description": "人员姓名"
          },
          {
            "name": "age",
            "type": "INTEGER",
            "required": false,
            "description": "年龄"
          }
        ]
      },
      {
        "name": "Company",
        "description": "公司节点",
        "propertiesCount": 6,
        "properties": [...]
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 4.2 获取边类型列表

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/edge-types`

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 3,
    "list": [
      {
        "name": "KNOWS",
        "description": "认识关系",
        "fromNodeType": null,
        "toNodeType": null,
        "propertiesCount": 2,
        "properties": [...]
      },
      {
        "name": "WORKS_FOR",
        "description": "工作关系",
        "fromNodeType": "Person",
        "toNodeType": "Company",
        "propertiesCount": 3,
        "properties": [...]
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 4.3 创建节点类型

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/node-types`

**请求参数**：
```json
{
  "name": "Organization",
  "description": "组织机构节点",
  "properties": [
    {
      "name": "orgName",
      "type": "STRING",
      "required": true,
      "description": "组织名称",
      "defaultValue": null
    },
    {
      "name": "industry",
      "type": "STRING",
      "required": false,
      "description": "所属行业",
      "defaultValue": "IT"
    },
    {
      "name": "foundedYear",
      "type": "INTEGER",
      "required": false,
      "description": "成立年份",
      "defaultValue": 2020
    }
  ]
}
```

### 4.4 创建边类型

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/edge-types`

**请求参数**：
```json
{
  "name": "INVESTED_IN",
  "description": "投资关系",
  "fromNodeType": "Person",
  "toNodeType": "Company",
  "properties": [
    {
      "name": "investmentAmount",
      "type": "DOUBLE",
      "required": false,
      "description": "投资金额",
      "defaultValue": null
    },
    {
      "name": "investmentDate",
      "type": "DATE",
      "required": false,
      "description": "投资日期",
      "defaultValue": null
    }
  ]
}
```

### 4.5 更新节点类型

**接口地址**：`PUT /api/v1/connections/{connectionId}/graphs/{graphName}/node-types/{typeName}`

### 4.6 删除节点类型

**接口地址**：`DELETE /api/v1/connections/{connectionId}/graphs/{graphName}/node-types/{typeName}`

## 5. 数据操作API

### 5.1 查询节点

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/vertices`

**查询参数**：
- `page`: 页码（默认1）
- `pageSize`: 每页大小（默认20）
- `nodeType`: 节点类型筛选（可选）
- `keyword`: 关键词搜索（可选）
- `sortBy`: 排序字段（可选，默认createdAt）
- `sortOrder`: 排序方向（ASC或DESC，默认DESC）

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 15234,
    "pageNum": 1,
    "pageSize": 20,
    "list": [
      {
        "uid": "v_001",
        "tagName": "Person",
        "properties": {
          "name": "张三",
          "age": 30,
          "city": "北京"
        },
        "createdAt": 1704748800000,
        "updatedAt": 1704748820000
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 5.2 查询边

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/edges`

**查询参数**：
- `page`: 页码（默认1）
- `pageSize`: 每页大小（默认20）
- `edgeType`: 边类型筛选（可选）
- `sourceUid`: 起始节点UID（可选）
- `targetUid`: 目标节点UID（可选）
- `keyword`: 关键词搜索（可选）
- `sortBy`: 排序字段（可选）
- `sortOrder`: 排序方向（可选）

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 45678,
    "pageNum": 1,
    "pageSize": 20,
    "list": [
      {
        "uid": "e_001",
        "edgeType": "KNOWS",
        "sourceUid": "v_001",
        "targetUid": "v_002",
        "properties": {
          "since": 2010,
          "relationship": "friend"
        },
        "createdAt": 1704748800000,
        "updatedAt": 1704748820000
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 5.3 创建节点

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/vertices`

**请求参数**：
```json
{
  "tagName": "Person",
  "properties": {
    "name": "李四",
    "age": 28,
    "city": "上海"
  }
}
```

**响应示例**：
```json
{
  "code": 201,
  "message": "节点创建成功",
  "data": {
    "uid": "v_003",
    "tagName": "Person",
    "properties": {
      "name": "李四",
      "age": 28,
      "city": "上海"
    },
    "createdAt": 1704748800000,
    "updatedAt": 1704748800000
  },
  "timestamp": 1704748800000
}
```

### 5.4 创建边

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/edges`

**请求参数**：
```json
{
  "edgeType": "KNOWS",
  "sourceUid": "v_001",
  "targetUid": "v_003",
  "properties": {
    "since": 2015
    "relationship": "colleague"
  }
}
```

**响应示例**：
```json
{
  "code": 201,
  "message": "边创建成功",
  "data": {
    "uid": "e_004",
    "edgeType": "KNOWS",
    "sourceUid": "v_001",
    "targetUid": "v_003",
    "properties": {
      "since": 2015,
      "relationship": "colleague"
    },
    "createdAt": 1704748800000,
    "updatedAt": 1704748800000
  },
  "timestamp": 1704748800000
}
```

### 5.5 更新节点

**接口地址**：`PUT /api/v1/connections/{connectionId}/graphs/{graphName}/vertices/{uid}`

**请求参数**：
```json
{
  "properties": {
    "age": 29,
    "city": "深圳"
  }
}
```

### 5.6 更新边

**接口地址**：`PUT /api/v1/connections/{connectionId}/graphs/{graphName}/edges/{uid}`

**请求参数**：
```json
{
  "properties": {
    "since": 2016
    "relationship": "senior_colleague"
  }
}
```

### 5.7 删除节点

**接口地址**：`DELETE /api/v1/connections/{connectionId}/graphs/{graphName}/vertices/{uid}?cascadeDelete=true`

**查询参数**：
- `cascadeDelete`: 是否级联删除关联边（默认true）

**响应示例**：
```json
{
  "code": 200,
  "message": "节点删除成功",
  "data": {
    "deletedVertexUid": "v_001",
    "deletedEdgeCount": 3
  },
  "timestamp": 1704748800000
}
```

### 5.8 删除边

**接口地址**：`DELETE /api/v1/connections/{connectionId}/graphs/{graphName}/edges/{uid}`

**响应示例**：
```json
{
  "code": 200,
  "message": "边删除成功",
  "data": {
    "deletedEdgeUid": "e_001"
  },
  "timestamp": 1704748800000
}
```

### 5.9 批量删除节点

**接口地址**：`DELETE /api/v1/connections/{connectionId}/graphs/{graphName}/vertices/batch`

**请求参数**：
```json
{
  "uids": ["v_001", "v_002", "v_003"],
  "cascadeDelete": true
}
```

### 5.10 批量删除边

**接口地址**：`DELETE /api/v1/connections/{connectionId}/graphs/{graphName}/edges/batch`

**请求参数**：
```json
{
  "uids": ["e_001", "e_002", "e_003"]
}
```

## 6. 导入导出API

### 6.1 CSV导入节点

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/import/vertices`

**请求参数**：
```json
{
  "fileData": "uid,tagName,name,age,city\nv_004,Person,王五,25,深圳",
  "fieldMapping": {
    "uid": 0,
    "tagName": 1,
    "name": 2,
    "age": 3,
    "city": 4
  },
  "batchSize": 100,
  "onError": "CONTINUE",
  "checkDuplicate": true
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "导入成功",
  "data": {
    "totalRows": 1000,
    "successCount": 995,
    "failureCount": 5,
    "errorLog": [
      {
        "row": 23,
        "error": "节点类型不存在：UnknownType"
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 6.2 CSV导入边

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/import/edges`

**请求参数**：
```json
{
  "fileData": "uid,edgeType,sourceUid,targetUid,relationship\ne_001,KNOWS,v_001,v_002,colleague",
  "fieldMapping": {
    "uid": 0,
    "edgeType": 1,
    "sourceUid": 2,
    "targetUid": 3,
    "relationship": 4
  },
  "batchSize": 100
}
```

### 6.3 导出节点CSV

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/export/vertices`

**查询参数**：
- `nodeType`: 节点类型筛选（可选）
- `fields`: 导出字段（可选，默认导出所有字段）

**响应**：返回CSV文件

### 6.4 导出边CSV

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/export/edges`

**查询参数**：
- `edgeType`: 边类型筛选（可选）
- `fields`: 导出字段（可选）

**响应**：返回CSV文件

### 6.5 导出图数据JSON

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/export/json`

**响应示例**：
```json
{
  "code": 200,
  "message": "导出成功",
  "data": {
    "vertices": [...],
    "edges": [...],
    "schema": {
      "nodeTypes": [...],
      "edgeTypes": [...]
    }
  },
  "timestamp": 1704748800000
}
```

## 7. 统计分析API

### 7.1 获取图统计

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/statistics`

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "vertexCount": 15234,
    "edgeCount": 45678,
    "nodeTypeDistribution": {
      "Person": 8500,
      "Company": 3200,
      "Organization": 1200
    },
    "edgeTypeDistribution": {
      "KNOWS": 23456,
      "WORKS_FOR": 18000,
      "INVESTED_IN": 4222
    },
    "averageDegree": 6.0,
    "maxDegree": 150,
    "connectedComponents": 5,
    "density": 0.0004
  },
  "timestamp": 1704748800000
}
```

## 8. 原生查询API

### 8.1 执行原生查询

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/query`

**请求参数**：
```json
{
  "query": "MATCH (p:Person)-[r:KNOWS]->(f:Person) WHERE p.name = '张三' RETURN p, r, f LIMIT 100",
  "timeout": 60,
  "limit": 1000,
  "explain": false
}
```

**查询参数说明**：
- `query`：原生查询语句（必填）
  - Neo4j：Cypher查询语言
  - NebulaGraph：nGQL查询语言
  - JanusGraph：Gremlin查询语言
- `timeout`：查询超时时间（单位：秒，默认60）
  - 可选值：30, 60, 120, 300
- `limit`：返回结果集限制（默认1000）
  - 可选值：100, 500, 1000, 5000
- `explain`：是否返回查询执行计划（默认false）

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "executionTimeMs": 234,
    "resultCount": 85,
    "columns": ["p", "r", "f"],
    "rows": [
      {
        "p": {
          "uid": "v_001",
          "tagName": "Person",
          "properties": {
            "name": "张三",
            "age": 30
          }
        },
        "r": {
          "uid": "e_001",
          "edgeType": "KNOWS",
          "sourceUid": "v_001",
          "targetUid": "v_002",
          "properties": {
            "since": 2010
          }
        },
        "f": {
          "uid": "v_002",
          "tagName": "Person",
          "properties": {
            "name": "李四",
            "age": 28
          }
        }
      }
    ],
    "explain": null
  },
  "timestamp": 1704748800000
}
```

### 8.2 查询执行计划

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/query/explain`

**请求参数**：
```json
{
  "query": "MATCH (p:Person)-[r:KNOWS]->(f:Person) WHERE p.name = '张三' RETURN p, r, f"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "查询计划获取成功",
  "data": {
    "plan": {
      "operator": "NodeByLabelScan",
      "labels": ["Person"],
      "estimatedRows": 15234,
      "dbHits": 15000,
      "children": [
        {
          "operator": "Filter",
          "expression": "p.name = '张三'",
          "estimatedRows": 100,
          "children": [
            {
              "operator": "Expand",
              "relationshipPattern": "(:Person)-[:KNOWS]->(:Person)",
              "estimatedRows": 300,
              "dbHits": 23456
            }
          ]
        }
      ]
    },
    "totalCost": 1.5,
    "totalRows": 100,
    "optimizationSuggestions": [
      "建议为Person.name属性创建索引以提升查询性能"
    ]
  },
  "timestamp": 1704748800000
}
```

### 8.3 获取查询模板

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/query/templates`

**查询参数**：
- `category`：模板分类（可选）
  - 基础查询：`basic`
  - 聚合查询：`aggregation`
  - 图算法：`algorithm`

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "templates": [
      {
        "id": "tpl_001",
        "name": "查询所有Person节点",
        "category": "basic",
        "description": "查询图中所有Person类型的节点",
        "query": "MATCH (p:Person) RETURN p LIMIT 100",
        "databaseType": "NEO4J"
      },
      {
        "id": "tpl_002",
        "name": "两度邻居查询",
        "category": "basic",
        "description": "查询节点的两度邻居",
        "query": "MATCH (p1)-[r1]->(p2)-[r2]->(p3) WHERE id(p1) = 'v_001' RETURN p1, r1, p2, r2, p3",
        "databaseType": "NEO4J"
      },
      {
        "id": "tpl_003",
        "name": "节点数量统计",
        "category": "aggregation",
        "description": "统计各节点类型的数量",
        "query": "MATCH (p) RETURN p.tagName, count(p) ORDER BY count(p) DESC",
        "databaseType": "NEO4J"
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 8.4 获取查询历史

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/query/history`

**查询参数**：
- `page`: 页码（默认1）
- `pageSize`: 每页大小（默认20）
- `startTime`: 开始时间（可选）
- `endTime`: 结束时间（可选）

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "list": [
      {
        "id": "query_001",
        "query": "MATCH (p:Person)-[r:KNOWS]->(f:Person) WHERE p.name = '张三' RETURN p, r, f",
        "executionTimeMs": 234,
        "resultCount": 85,
        "executedAt": 1704748800000,
        "status": "SUCCESS"
      },
      {
        "id": "query_002",
        "query": "MATCH (p:Person) RETURN count(p)",
        "executionTimeMs": 123,
        "resultCount": 1,
        "executedAt": 1704748700000,
        "status": "SUCCESS"
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 8.5 删除查询历史

**接口地址**：`DELETE /api/v1/connections/{connectionId}/graphs/{graphName}/query/history`

**请求参数**：
```json
{
  "ids": ["query_001", "query_002"]
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": {
    "deletedCount": 2
  },
  "timestamp": 1704748800000
}
```

### 8.6 批量查询

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/query/batch`

**请求参数**：
```json
{
  "queries": [
    {
      "id": "query_001",
      "query": "MATCH (p:Person) RETURN count(p) AS count"
    },
    {
      "id": "query_002",
      "query": "MATCH ()-[r:KNOWS]->() RETURN count(r) AS count"
    },
    {
      "id": "query_003",
      "query": "MATCH (p:Person) RETURN p LIMIT 10"
    }
  ],
  "timeout": 60,
  "continueOnError": true
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "批量查询完成",
  "data": {
    "results": [
      {
        "id": "query_001",
        "status": "SUCCESS",
        "executionTimeMs": 123,
        "resultCount": 1,
        "rows": [
          {
            "count": 15234
          }
        ]
      },
      {
        "id": "query_002",
        "status": "SUCCESS",
        "executionTimeMs": 89,
        "resultCount": 1,
        "rows": [
          {
            "count": 45678
          }
        ]
      },
      {
        "id": "query_003",
        "status": "SUCCESS",
        "executionTimeMs": 234,
        "resultCount": 10,
        "rows": [...]
      }
    ],
    "summary": {
      "totalQueries": 3,
      "successCount": 3,
      "failureCount": 0,
      "totalExecutionTimeMs": 446
    }
  },
  "timestamp": 1704748800000
}
```

### 8.7 保存查询脚本

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/query/scripts`

**请求参数**：
```json
{
  "name": "社交网络分析脚本",
  "description": "用于分析社交网络图数据的查询脚本",
  "queries": [
    "MATCH (p:Person) RETURN count(p) AS personCount",
    "MATCH ()-[r:KNOWS]->() RETURN count(r) AS knowsCount",
    "MATCH (p:Person) RETURN p.name, p.age ORDER BY p.age DESC LIMIT 10"
  ]
}
```

**响应示例**：
```json
{
  "code": 201,
  "message": "查询脚本保存成功",
  "data": {
    "scriptId": "script_001",
    "name": "社交网络分析脚本",
    "description": "用于分析社交网络图数据的查询脚本",
    "queryCount": 3,
    "createdAt": 1704748800000
  },
  "timestamp": 1704748800000
}
```

### 8.8 获取查询脚本列表

**接口地址**：`GET /api/v1/connections/{connectionId}/graphs/{graphName}/query/scripts`

**查询参数**：
- `page`: 页码（默认1）
- `pageSize`: 每页大小（默认20）
- `keyword`: 关键词搜索（可选）

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 10,
    "pageNum": 1,
    "pageSize": 20,
    "list": [
      {
        "scriptId": "script_001",
        "name": "社交网络分析脚本",
        "description": "用于分析社交网络图数据的查询脚本",
        "queryCount": 3,
        "createdAt": 1704748800000,
        "updatedAt": 1704748900000
      }
    ]
  },
  "timestamp": 1704748800000
}
```

### 8.9 执行查询脚本

**接口地址**：`POST /api/v1/connections/{connectionId}/graphs/{graphName}/query/scripts/{scriptId}/execute`

**响应示例**：
```json
{
  "code": 200,
  "message": "查询脚本执行完成",
  "data": {
    "scriptId": "script_001",
    "scriptName": "社交网络分析脚本",
    "results": [
      {
        "queryIndex": 0,
        "query": "MATCH (p:Person) RETURN count(p) AS personCount",
        "status": "SUCCESS",
        "executionTimeMs": 123,
        "resultCount": 1
      },
      {
        "queryIndex": 1,
        "query": "MATCH ()-[r:KNOWS]->() RETURN count(r) AS knowsCount",
        "status": "SUCCESS",
        "executionTimeMs": 89,
        "resultCount": 1
      }
    ],
    "summary": {
      "totalQueries": 3,
      "successCount": 3,
      "failureCount": 0,
      "totalExecutionTimeMs": 446
    }
  },
  "timestamp": 1704748800000
}
```

### 8.10 查询错误码

**原生查询错误码**：

| 错误码 | 说明 | HTTP状态码 |
|-------|------|----------|
| 45001 | 查询语句为空 | 400 |
| 45002 | 查询语法错误 | 400 |
| 45003 | 查询超时 | 408 |
| 45004 | 查询结果超限 | 400 |
| 45005 | 查询复杂度超限 | 400 |
| 45006 | 图数据库不支持该查询语法 | 400 |
| 45007 | 查询执行失败 | 500 |
| 45008 | 查询注入风险 | 400 |

## 9. 系统管理API

### 8.1 健康检查

**接口地址**：`GET /api/v1/health`

**响应示例**：
```json
{
  "code": 200,
  "message": "系统正常",
  "data": {
    "status": "UP",
    "version": "1.0.0",
    "timestamp": 1704748800000,
    "services": {
      "mysql": "UP",
      "redis": "UP"
    }
  }
}
```

### 8.2 获取操作日志

**接口地址**：`GET /api/v1/connections/{connectionId}/logs`

**查询参数**：
- `page`: 页码（默认1）
- `pageSize`: 每页大小（默认20）
- `startTime`: 开始时间（可选）
- `endTime`: 结束时间（可选）
- `operationType`: 操作类型筛选（可选）
- `resultStatus`: 结果状态筛选（可选）

**响应示例**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "list": [
      {
        "id": 123456,
        "connectionId": "conn_001",
        "graphName": "neo4j",
        "operationType": "QUERY",
        "operationTarget": "NODE",
        "resultStatus": "SUCCESS",
        "executionTimeMs": 234,
        "affectedRows": 100,
        "createdAt": 1704748800000
      }
    ]
  },
  "timestamp": 1704748800000
}
```

## 9. API安全

### 9.1 认证方式

**JWT Token认证**：
```
Authorization: Bearer <JWT_TOKEN>
```

### 9.2 请求头

```
Content-Type: application/json
Accept: application/json
Authorization: Bearer <JWT_TOKEN>
X-Request-ID: <唯一请求ID>
X-Client-Version: 1.0.0
```

### 9.3 限流策略

- 同一IP每分钟最多100次请求
- 同一用户每分钟最多200次请求
- 超出限制返回429错误

### 9.4 CORS配置

允许的Origin：所有（生产环境可配置具体域名）
允许的Methods：GET, POST, PUT, DELETE, PATCH
允许的Headers：Content-Type, Authorization
允许的Credentials：true

## 10. API文档

使用**Swagger/Springdoc**自动生成API文档：

**访问地址**：`http://localhost:8080/swagger-ui.html`

**注解使用**：
```java
@RestController
@RequestMapping("/api/v1")
@Api(tags = "连接管理")
public class ConnectionController {
    
    @Operation(summary = "创建连接", description = "创建新的图数据库连接")
    @PostMapping("/connections")
    public ResponseEntity<ApiResponse<ConnectionVO>> createConnection(
        @RequestBody @Valid ConnectionDTO connectionDTO) {
        // 实现
    }
}
```

## 11. 错误处理

### 11.1 全局异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(
            BusinessException e) {
        ApiResponse<?> response = new ApiResponse<>(
            e.getCode(),
            e.getMessage(),
            null,
            System.currentTimeMillis()
        );
        return ResponseEntity.status(getHttpStatus(e.getCode()))
                           .body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException e) {
        // 处理参数校验失败
    }
}
```

### 11.2 响应状态码映射

```java
private HttpStatus getHttpStatus(String code) {
    int codeInt = Integer.parseInt(code);
    if (codeInt >= 40000 && codeInt < 50000) {
        return HttpStatus.BAD_REQUEST;
    } else {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
```
