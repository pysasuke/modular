package com.py.modular.common.kafka;

/**
 * Kafka Consumer回调类
 *
 * @author PYSASUKE
 */
public interface MessageConsumerCallback {

    /**
     * 收到消息回调
     *
     * @param key
     * @param value
     * @param topic
     * @param partition
     */
    void onMessage(String key, byte[] value, String topic, int partition);
}
