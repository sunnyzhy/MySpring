package org.example.config;

/**
 * @author zhy
 * @date 2024/9/23 13:48
 */
public enum ConstantSet {
    SIGN_SIGN(null,"sign"),
    SIGN_TIMESTAMP(null,"timestamp"),
    SIGN_NONCE(null,"nonce"),
    SIGN_SECRET(null,"secret"),
    EQUAL(null,"="),
    AND(null,"&"),
    UNDEFINED(null,"undefined"),
    NULL(null,"null"),
    SIGN_ERROR(40001,"无效的签名"),
    SIGN_REDIS_HASH_KEY_FORMAT(null,"sign:%s");
    private Integer code;
    private String name;

    ConstantSet(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
