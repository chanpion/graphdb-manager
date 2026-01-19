# MyBatis-Plus 配置说明

## 1. 概述

本系统使用 **MyBatis-Plus 3.5.5** 作为ORM框架，替代传统的JPA。MyBatis-Plus是基于MyBatis的增强工具，提供了更加便捷的CRUD操作和强大的代码生成功能。

## 2. 依赖配置

### 2.1 Maven依赖

在父POM文件中添加版本管理：

```xml
<properties>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
</properties>
```

在storage模块的POM文件中添加依赖：

```xml
<dependencies>
    <!-- MyBatis-Plus Starter -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>${mybatis-plus.version}</version>
    </dependency>
    
    <!-- MyBatis-Plus 代码生成器（可选） -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-generator</artifactId>
        <version>${mybatis-plus.version}</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## 3. 配置文件

### 3.1 application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/graphdb_manager?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000

# MyBatis-Plus配置
mybatis-plus:
  # Mapper XML文件位置
  mapper-locations: classpath*:/mapper/**/*.xml
  # 实体类包路径
  type-aliases-package: com.graphmanager.storage.entity
  # 配置
  configuration:
    # 驼峰下划线转换
    map-underscore-to-camel-case: true
    # 日志输出
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
    # 开启二级缓存
    cache-enabled: true
    # 懒加载
    lazy-loading-enabled: false
  # 全局配置
  global-config:
    # 数据库相关配置
    db-config:
      # 主键类型：AUTO-数据库自增，ASSIGN_ID-雪花算法
      id-type: auto
      # 逻辑删除字段
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      # 表名前缀
      table-prefix: ""
    # Banner配置
    banner: false
```

## 4. 核心类实现

### 4.1 实体类（Entity）

#### ConnectionEntity.java

```java
package com.graphmanager.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 连接配置实体类
 */
@Data
@TableName("connection_config")
public class ConnectionEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 连接名称
     */
    private String name;

    /**
     * 数据库类型：NEO4J/NEBULA/JANUS
     */
    private String databaseType;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String passwordEncrypted;

    /**
     * 数据库名/图空间
     */
    private String databaseName;

    /**
     * 存储类型（仅JANUS）：HBASE/CASSANDRA
     */
    private String storageType;

    /**
     * 存储配置参数（仅JANUS）
     */
    private String storageConfig;

    /**
     * 额外连接参数（JSON格式）
     */
    private String extraParams;

    /**
     * 连接状态：0-未测试，1-正常，2-异常
     */
    private Integer status;

    /**
     * 连接描述
     */
    private String description;

    /**
     * 优先级：1-10，数字越小优先级越高
     */
    private Integer priority;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

#### GraphInstanceEntity.java

```java
package com.graphmanager.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 图实例实体类
 */
@Data
@TableName("graph_instance")
public class GraphInstanceEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联连接ID
     */
    private Long connectionId;

    /**
     * 图名称
     */
    private String graphName;

    /**
     * 数据库类型：NEO4J/NEBULA/JANUS
     */
    private String databaseType;

    /**
     * 节点数量
     */
    private Long vertexCount;

    /**
     * 边数量
     */
    private Long edgeCount;

    /**
     * 图状态：NORMAL/ARCHIVED
     */
    private String status;

    /**
     * 图描述
     */
    private String description;

    /**
     * Schema版本号
     */
    private String schemaVersion;

    /**
     * Schema缓存时间
     */
    private LocalDateTime cacheTime;

    /**
     * 是否默认图
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 最后访问时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastAccessedAt;
}
```

### 4.2 Mapper接口

#### ConnectionMapper.java

```java
package com.graphmanager.storage.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graphmanager.storage.entity.ConnectionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 连接配置Mapper接口
 */
@Mapper
public interface ConnectionMapper extends BaseMapper<ConnectionEntity> {

    /**
     * 根据状态查询连接列表
     * @param status 连接状态
     * @return 连接列表
     */
    @Select("SELECT * FROM connection_config WHERE status = #{status} ORDER BY created_at DESC")
    List<ConnectionEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据数据库类型查询连接列表
     * @param databaseType 数据库类型
     * @return 连接列表
     */
    @Select("SELECT * FROM connection_config WHERE database_type = #{databaseType} ORDER BY priority ASC")
    List<ConnectionEntity> selectByDatabaseType(@Param("databaseType") String databaseType);

    /**
     * 根据名称查询连接
     * @param name 连接名称
     * @return 连接实体
     */
    @Select("SELECT * FROM connection_config WHERE name = #{name} LIMIT 1")
    ConnectionEntity selectByName(@Param("name") String name);
}
```

#### GraphInstanceMapper.java

```java
package com.graphmanager.storage.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graphmanager.storage.entity.GraphInstanceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 图实例Mapper接口
 */
@Mapper
public interface GraphInstanceMapper extends BaseMapper<GraphInstanceEntity> {

    /**
     * 根据连接ID查询图实例列表
     * @param connectionId 连接ID
     * @return 图实例列表
     */
    @Select("SELECT * FROM graph_instance WHERE connection_id = #{connectionId} ORDER BY updated_at DESC")
    List<GraphInstanceEntity> selectByConnectionId(@Param("connectionId") Long connectionId);

    /**
     * 根据连接ID和图名称查询图实例
     * @param connectionId 连接ID
     * @param graphName 图名称
     * @return 图实例
     */
    @Select("SELECT * FROM graph_instance WHERE connection_id = #{connectionId} AND graph_name = #{graphName} LIMIT 1")
    GraphInstanceEntity selectByConnectionIdAndGraphName(@Param("connectionId") Long connectionId, 
                                                     @Param("graphName") String graphName);
}
```

### 4.3 MyBatis-Plus配置类

#### MyBatisPlusConfig.java

```java
package com.graphmanager.storage.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus拦截器配置
     * 添加分页插件、乐观锁插件、防止全表更新插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(1000L); // 单页最大数量限制
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 防止全表更新和删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }
}
```

### 4.4 字段自动填充处理器

#### MetaObjectHandler.java

```java
package com.graphmanager.storage.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * 字段自动填充处理器
 */
@Component
public class MetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 4.5 启动类配置

在主启动类上添加Mapper扫描注解：

```java
package com.graphmanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.graphmanager.storage.repository") // 扫描Mapper接口
public class GraphManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GraphManagerApplication.class, args);
    }
}
```

## 5. 使用示例

### 5.1 Service层使用Mapper

```java
package com.graphmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graphmanager.storage.entity.ConnectionEntity;
import com.graphmanager.storage.repository.ConnectionMapper;
import com.graphmanager.service.ConnectionService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConnectionServiceImpl extends ServiceImpl<ConnectionMapper, ConnectionEntity> 
    implements ConnectionService {

    /**
     * 分页查询连接列表
     */
    @Override
    public Page<ConnectionEntity> pageList(int pageNum, int pageSize) {
        Page<ConnectionEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ConnectionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ConnectionEntity::getCreatedAt);
        return this.page(page, wrapper);
    }

    /**
     * 根据状态查询连接列表
     */
    @Override
    public List<ConnectionEntity> listByStatus(Integer status) {
        LambdaQueryWrapper<ConnectionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConnectionEntity::getStatus, status);
        return this.list(wrapper);
    }

    /**
     * 根据ID查询连接
     */
    @Override
    public ConnectionEntity getById(Long id) {
        return super.getById(id);
    }

    /**
     * 创建连接
     */
    @Override
    public boolean createConnection(ConnectionEntity entity) {
        return this.save(entity);
    }

    /**
     * 更新连接
     */
    @Override
    public boolean updateConnection(ConnectionEntity entity) {
        return this.updateById(entity);
    }

    /**
     * 删除连接
     */
    @Override
    public boolean deleteConnection(Long id) {
        return this.removeById(id);
    }
}
```

## 6. 注意事项

1. **字段命名**：数据库表字段使用下划线命名（如：created_at），实体类使用驼峰命名（如：createdAt），MyBatis-Plus会自动转换。
2. **主键策略**：使用AUTO策略，依赖数据库自增。
3. **字段填充**：插入和更新时自动填充createdAt和updatedAt字段。
4. **分页查询**：使用PaginationInnerInterceptor插件，Page对象进行分页。
5. **Mapper扫描**：必须在主启动类上添加@MapperScan注解。
6. **日志配置**：开发环境可以开启SQL日志，生产环境建议关闭。
