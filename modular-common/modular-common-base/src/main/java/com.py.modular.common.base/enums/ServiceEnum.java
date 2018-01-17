package com.py.modular.common.base.enums;

/**
 * Service枚举
 *
 * @author PYSASUKE
 * @date 2018/1/9
 */
public enum ServiceEnum {
    /**
     * 初始操作
     */
    ZERO(0, "初始操作"),
    /**
     * 服务一
     */
    FIRST(1, "服务一"),
    /**
     * 服务二
     */
    SECOND(2, "服务二"),
    /**
     * 服务三
     */
    THIRD(3, "服务三");

    public final int code;

    public final String msg;

    ServiceEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
