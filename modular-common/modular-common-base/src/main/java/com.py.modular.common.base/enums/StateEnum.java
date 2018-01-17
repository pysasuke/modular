package com.py.modular.common.base.enums;

/**
 * 状态枚举
 *
 * @author PYSASUKE
 * @date 2018/1/9
 */
public enum StateEnum {
    /**
     * 初始状态
     */
    UNDO(0, "初始状态"),
    /**
     * 成功
     */
    SUCCESS(1, "成功");

    public final int code;

    public final String msg;

    StateEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
