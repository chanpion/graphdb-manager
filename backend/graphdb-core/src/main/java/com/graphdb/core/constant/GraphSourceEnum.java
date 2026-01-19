package com.graphdb.core.constant;

/**
 * 图来源枚举
 * 标识图是由平台创建还是图数据库已有
 */
public enum GraphSourceEnum {
    /**
     * 平台创建
     */
    PLATFORM("PLATFORM", "平台创建"),
    
    /**
     * 图数据库已有
     */
    EXISTING("EXISTING", "图数据库已有");
    
    private final String code;
    private final String desc;
    
    GraphSourceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据代码获取枚举
     * @param code 枚举代码
     * @return 对应的枚举
     * @throws IllegalArgumentException 如果代码无效
     */
    public static GraphSourceEnum fromCode(String code) {
        for (GraphSourceEnum source : values()) {
            if (source.code.equals(code)) {
                return source;
            }
        }
        throw new IllegalArgumentException("无效的图来源代码: " + code);
    }
    
    /**
     * 检查代码是否有效
     * @param code 待检查的代码
     * @return 如果有效返回true，否则false
     */
    public static boolean isValid(String code) {
        for (GraphSourceEnum source : values()) {
            if (source.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}