package com.graphdb.core.constant;

/**
 * 数据库类型枚举（与common模块保持一致）
 */
public enum DatabaseTypeEnum {
    NEO4J("Neo4j"),
    NEBULA("NebulaGraph"),
    JANUS("JanusGraph");
    
    private final String name;
    
    DatabaseTypeEnum(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public static DatabaseTypeEnum fromCode(String code) {
        for (DatabaseTypeEnum type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid database type: " + code);
    }
}