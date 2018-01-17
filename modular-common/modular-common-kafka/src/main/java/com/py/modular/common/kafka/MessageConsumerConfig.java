package com.py.modular.common.kafka;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Kafka consumer配置
 *
 * @author PYSASUKE
 */
@Data
@Builder
public class MessageConsumerConfig {

    /**
     * 订阅topic列表
     */
    private List<String> topics;

    /**
     * 消息到达回调类实例
     */
    private MessageConsumerCallback consumerCallback;

    /**
     * kafka集群地址（格式: "地址:端口,地址:端口"）
     */
    private String bootstrapServers;

    /**
     * group.id
     */
    private String groupId;

    /**
     * client.id
     */
    private String clientId = "";

    /**
     * max.poll.records. The maximum number of records returned in a single call to poll()
     */
    private int maxPollRecords = 4096;
}
