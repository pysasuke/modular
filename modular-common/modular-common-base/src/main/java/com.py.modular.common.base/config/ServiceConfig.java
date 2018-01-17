package com.py.modular.common.base.config;

import lombok.Builder;
import lombok.Data;

/**
 * Service配置
 *
 * @author PYSASUKE
 */
@Data
@Builder
public class ServiceConfig {

    /**
     * 服务标识
     */
    private String serviceIdFlag;

    /**
     * kafka broker地址
     */
    private String bootstrapServers;

    /**
     * 从kafka中获取消息批次大小
     */
    private int maxPollRecords;

    /**
     * 任务线程池最小线程数
     */
    private int minTaskThreadPoolSize;

    /**
     * 任务线程池最大线程数
     */
    private int maxTaskThreadPoolSize;
}
