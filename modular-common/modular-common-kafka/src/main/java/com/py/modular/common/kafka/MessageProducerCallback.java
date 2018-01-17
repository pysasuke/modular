package com.py.modular.common.kafka;

/**
 * Kafka Producer回调类
 * @author PYSASUKE
 */
public interface MessageProducerCallback {

    /**
     * 发送成功回调
     * @param producer
     * @param key
     * @param value
     * @param partition
     * @param offset
     */
    void onSuccess(MessageProducer producer, String key, byte[] value, int partition, long offset);

    /**
     * 发送失败回调
     * @param producer
     * @param key
     * @param value
     * @param e
     */
    void onFailure(MessageProducer producer, String key, byte[] value, Exception e);
}
