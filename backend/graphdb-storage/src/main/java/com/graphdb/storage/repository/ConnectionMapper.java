package com.graphdb.storage.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graphdb.storage.entity.ConnectionEntity;
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
    @Select("SELECT * FROM connection_config WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<ConnectionEntity> selectByStatus(@Param("status") Integer status);
    
    /**
     * 根据数据库类型查询连接列表
     * @param databaseType 数据库类型
     * @return 连接列表
     */
    @Select("SELECT * FROM connection_config WHERE type = #{databaseType} AND deleted = 0 ORDER BY created_at DESC")
    List<ConnectionEntity> selectByDatabaseType(@Param("databaseType") String databaseType);
    
    /**
     * 根据名称查询连接
     * @param name 连接名称
     * @return 连接实体
     */
    @Select("SELECT * FROM connection_config WHERE name = #{name} AND deleted = 0 LIMIT 1")
    ConnectionEntity selectByName(@Param("name") String name);
}