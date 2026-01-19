package com.graphdb.core.interfaces;

import com.graphdb.core.constant.DatabaseTypeEnum;
import com.graphdb.core.exception.CoreException;
import com.graphdb.core.model.CsvImportConfig;
import java.io.InputStream;
import java.util.Map;
import java.util.List;

/**
 * 数据处理器接口
 * 负责节点和边的增删改查操作
 */
public interface DataHandler {
    
    /**
     * 创建节点
     * @param graphName 图名称
     * @param label 节点标签
     * @param properties 属性集合
     * @return 创建的节点（包含uid）
     * @throws CoreException 创建异常
     */
    Map<String, Object> createVertex(String graphName, String label, Map<String, Object> properties);
    
    /**
     * 创建边
     * @param graphName 图名称
     * @param label 边标签
     * @param sourceUid 源节点UID
     * @param targetUid 目标节点UID
     * @param properties 属性集合
     * @return 创建的边（包含uid）
     * @throws CoreException 创建异常
     */
    Map<String, Object> createEdge(String graphName, String label, 
                                          String sourceUid, String targetUid, 
                                          Map<String, Object> properties);
    
    /**
     * 删除节点
     * @param graphName 图名称
     * @param uid 节点UID
     * @throws CoreException 删除异常
     */
    void deleteVertex(String graphName, String uid);
    
    /**
     * 删除边
     * @param graphName 图名称
     * @param uid 边UID
     * @throws CoreException 删除异常
     */
    void deleteEdge(String graphName, String uid);
    
    /**
     * 查询节点
     * @param graphName 图名称
     * @param label 节点标签（可选）
     * @return 节点列表
     * @throws CoreException 查询异常
     */
    List<Map<String, Object>> queryVertices(String graphName, String label);
    
    /**
     * 查询边
     * @param graphName 图名称
     * @param label 边标签（可选）
     * @return 边列表
     * @throws CoreException 查询异常
     */
    List<Map<String, Object>> queryEdges(String graphName, String label);
    
    /**
     * 从CSV文件导入数据
     * @param graphName 图名称
     * @param config 导入配置
     * @param csvStream CSV文件输入流
     * @return 导入结果消息（如：导入成功X条节点，Y条边）
     * @throws CoreException 导入异常
     */
    String importFromCsv(String graphName, CsvImportConfig config, InputStream csvStream);
}
