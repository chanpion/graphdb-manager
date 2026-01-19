package com.graphdb.core.interfaces;

import com.graphdb.core.exception.CoreException;

import java.util.Map;

/**
 * Schema处理器接口
 * 负责图schema的查询和操作
 */
public interface SchemaHandler {
    
    /**
     * 获取图的所有节点类型
     * @param graphName 图名称
     * @return 节点类型列表（类型名 -> 属性列表）
     * @throws CoreException 查询异常
     */
    Map<String, Map<String, Object>> getNodeTypes(String graphName);
    
    /**
     * 获取图的所有边类型
     * @param graphName 图名称
     * @return 边类型列表（类型名 -> 属性列表）
     * @throws CoreException 查询异常
     */
    Map<String, Map<String, Object>> getEdgeTypes(String graphName);
    
    /**
     * 创建节点类型
     * @param graphName 图名称
     * @param typeName 节点类型名称
     * @param properties 属性定义（属性名 -> 属性类型）
     * @throws CoreException 创建异常
     */
    void createNodeType(String graphName, String typeName, Map<String, Object> properties);
    
    /**
     * 删除节点类型
     * @param graphName 图名称
     * @param typeName 节点类型名称
     * @throws CoreException 删除异常
     */
    void deleteNodeType(String graphName, String typeName);
}
