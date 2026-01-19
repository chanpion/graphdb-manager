package com.graphdb.storage.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graphdb.storage.entity.GraphInstanceEntity;
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
    @Select("SELECT * FROM graph_instance WHERE connection_id = #{connectionId} AND deleted = 0 ORDER BY updated_at DESC")
    List<GraphInstanceEntity> selectByConnectionId(@Param("connectionId") Long connectionId);
    
    /**
     * 根据连接ID和图名称查询图实例
     * @param connectionId 连接ID
     * @param graphName 图名称
     * @return 图实例
     */
    @Select("SELECT * FROM graph_instance WHERE connection_id = #{connectionId} AND graph_name = #{graphName} AND deleted = 0 LIMIT 1")
    GraphInstanceEntity selectByConnectionIdAndGraphName(@Param("connectionId") Long connectionId,
                                                     @Param("graphName") String graphName);

    /**
     * 根据连接ID和图来源查询图实例列表
     * @param connectionId 连接ID
     * @param sourceType 图来源
     * @return 图实例列表
     */
    @Select("SELECT * FROM graph_instance WHERE connection_id = #{connectionId} AND source_type = #{sourceType} AND deleted = 0 ORDER BY updated_at DESC")
    List<GraphInstanceEntity> selectByConnectionIdAndSourceType(@Param("connectionId") Long connectionId,
                                                               @Param("sourceType") String sourceType);

    /**
     * 根据图来源查询所有图实例列表
     * @param sourceType 图来源
     * @return 图实例列表
     */
    @Select("SELECT * FROM graph_instance WHERE source_type = #{sourceType} AND deleted = 0 ORDER BY updated_at DESC")
    List<GraphInstanceEntity> selectBySourceType(@Param("sourceType") String sourceType);
}
