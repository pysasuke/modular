package com.py.modular.common.kafka;

import lombok.Builder;
import lombok.Data;

/**
 * Kafka producer配置
 *
 * @author PYSASUKE
 */
@Data
@Builder
public class MessageProducerConfig {

    /**
     * topic名称
     */
    private String topic;

    /**
     * 回调类实例(异步发送时使用)
     */
    private MessageProducerCallback producerCallback;

    /**
     * kafka集群地址（格式: "地址:端口,地址:端口"）
     */
    private String bootstrapServers;

    /**
     * client.id
     */
    private String clientId;
}
