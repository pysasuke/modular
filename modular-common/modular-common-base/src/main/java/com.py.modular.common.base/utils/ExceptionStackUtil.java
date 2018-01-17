package com.py.modular.common.base.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 获取异常堆栈字符串
 *
 * @author PYSASUKE
 */
public class ExceptionStackUtil {
    /**
     * 打印异常栈信息
     * @param e
     * @return
     */
    public static String getExceptionStack(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
