package com.py.modular.common.base.constants;

/**
 * 消息队列名称
 *
 * @author PYSASUKE
 */
public class TopicName {

    /**
     * 请求队列
     */
    public static final String KAFKA_ZERO = "kafka-zero";
    public static final String KAFKA_FIRST = "kafka-first";
    public static final String KAFKA_SECOND = "kafka-second";

    private TopicName() {
        throw new IllegalStateException("该对象不能实例化");
    }
}
