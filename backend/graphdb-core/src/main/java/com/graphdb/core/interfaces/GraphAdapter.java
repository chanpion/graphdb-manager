package com.graphdb.core.interfaces;

import com.graphdb.core.constant.DatabaseTypeEnum;
import com.graphdb.core.model.ConnectionConfig;
import com.graphdb.core.model.GraphSchema;
import com.graphdb.core.model.LabelType;
import com.graphdb.core.exception.CoreException;
import java.util.List;

/**
 * 图数据库适配器统一接口
 * 所有图数据库实现必须实现此接口
 */
public interface GraphAdapter {
    
    /**
     * 获取适配器类型
     * @return 数据库类型
     */
    DatabaseTypeEnum getDatabaseType();
    
    /**
     * 测试连接
     * @param config 连接配置
     * @return 是否连接成功
     * @throws CoreException 连接异常
     */
    boolean testConnection(ConnectionConfig config) throws CoreException;
    
    /**
     * 建立连接
     * @param config 连接配置
     * @throws CoreException 连接异常
     */
    void connect(ConnectionConfig config) throws CoreException;
    
    /**
     * 断开连接
     */
    void disconnect();
    
    /**
     * 判断是否已连接
     * @return 连接状态
     */
    boolean isConnected();
    
    /**
     * 获取图列表
     * @param config 连接配置
     * @return 图名称列表
     * @throws CoreException 操作异常
     */
    List<String> getGraphs(ConnectionConfig config) throws CoreException;
    
    /**
     * 获取图schema
     * @param config 连接配置
     * @param graphName 图名称
     * @return 图schema信息
     * @throws CoreException 操作异常
     */
    GraphSchema getGraphSchema(ConnectionConfig config, String graphName) throws CoreException;
    
    /**
     * 创建图
     * @param config 连接配置
     * @param graphName 图名称
     * @throws CoreException 操作异常
     */
    void createGraph(ConnectionConfig config, String graphName) throws CoreException;
    
    /**
     * 删除图
     * @param config 连接配置
     * @param graphName 图名称
     * @throws CoreException 操作异常
     */
    void deleteGraph(ConnectionConfig config, String graphName) throws CoreException;
    
    /**
     * 创建点类型
     * @param config 连接配置
     * @param graphName 图名称
     * @param labelType 点类型定义
     * @throws CoreException 操作异常
     */
    void createVertexType(ConnectionConfig config, String graphName, LabelType labelType) throws CoreException;
    
    /**
     * 删除点类型
     * @param config 连接配置
     * @param graphName 图名称
     * @param labelName 点类型名称
     * @throws CoreException 操作异常
     */
    void deleteVertexType(ConnectionConfig config, String graphName, String labelName) throws CoreException;
    
    /**
     * 创建边类型
     * @param config 连接配置
     * @param graphName 图名称
     * @param labelType 边类型定义
     * @throws CoreException 操作异常
     */
    void createEdgeType(ConnectionConfig config, String graphName, LabelType labelType) throws CoreException;
    
    /**
     * 删除边类型
     * @param config 连接配置
     * @param graphName 图名称
     * @param labelName 边类型名称
     * @throws CoreException 操作异常
     */
    void deleteEdgeType(ConnectionConfig config, String graphName, String labelName) throws CoreException;
    
    /**
     * 获取点类型列表
     * @param config 连接配置
     * @param graphName 图名称
     * @return 点类型列表
     * @throws CoreException 操作异常
     */
    List<LabelType> getVertexTypes(ConnectionConfig config, String graphName) throws CoreException;
    
    /**
     * 获取边类型列表
     * @param config 连接配置
     * @param graphName 图名称
     * @return 边类型列表
     * @throws CoreException 操作异常
     */
    List<LabelType> getEdgeTypes(ConnectionConfig config, String graphName) throws CoreException;
}
