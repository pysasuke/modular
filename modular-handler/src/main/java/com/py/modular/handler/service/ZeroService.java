package com.py.modular.handler.service;

import com.py.modular.common.base.BaseService;
import com.py.modular.common.base.KafkaMessage.MessageType;
import com.py.modular.common.base.KafkaMessage.MessageWrap;
import com.py.modular.common.base.config.ServiceConfig;
import com.py.modular.common.base.constants.KafkaConstants;
import com.py.modular.common.base.constants.ServiceFlagConstants;
import com.py.modular.common.base.constants.TopicName;
import com.py.modular.common.base.utils.MessageUtil;
import com.py.modular.common.database.dao.RecordMapper;
import com.py.modular.common.database.entity.Record;
import com.py.modular.common.kafka.MessageProducer;
import com.py.modular.common.redis.dao.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



/**
 * 状态更新服务
 *
 * @author PYSASUKE
 */
@Service
public class ZeroService extends BaseService {
    /**
     * 请求暂停服务
     */
    private volatile boolean stopServiceRequested = false;

    /**
     * 服务标识
     */
    private static final String SERVICE_ID_FLAG = ServiceFlagConstants.SERVICE_FLAG_KAFKA_ZARO;

    /**
     * 消息队列发送线程池
     */
    private ThreadPoolExecutor messageSendThreadPool;
    /**
     * 请求队列producer
     */
    private MessageProducer reqProducer;
    private final RecordMapper recordMapper;
    private final RedisService redisService;

    @Autowired
    public ZeroService(RecordMapper recordMapper,
                       RedisService redisService) {
        this.redisService = redisService;
        this.recordMapper = recordMapper;
        ServiceConfig serviceConfig = ServiceConfig.builder()
                .serviceIdFlag(SERVICE_ID_FLAG)
                .bootstrapServers(KafkaConstants.BOOTSTRAP_SERVERS)
                .build();
        setServiceConfig(serviceConfig);

        startService();
    }

    /**
     * 启动服务
     */
    @Override
    public void startService() {
        initSendMessageThreadPool();
        reqProducer = createProducer(TopicName.KAFKA_ZERO, "zero");

    }

    /**
     * 创建消息队列发送线程池
     */
    private void initSendMessageThreadPool() {
        messageSendThreadPool = new ThreadPoolExecutor(
                20,
                100,
                Long.MAX_VALUE,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(10000),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    @Override
    public void onSuccess(MessageProducer producer, String key, byte[] value, int partition, long offset) {

    }

    @Override
    public void onFailure(MessageProducer producer, String key, byte[] value, Exception e) {

    }

    @Override
    public void onMessage(String key, byte[] value, String topic, int partition) {

    }

    /**
     * 状态处理
     */
    private class StateHandler implements Runnable {
        private Record record;

        StateHandler(Record record) {
            this.record = record;
        }

        @Override
        public void run() {
            recordMapper.insert(record);
            Record redisRecord = recordMapper.selectById(record.getId());
            redisService.set("record:" + Long.toString(record.getId()), redisRecord);
            sendReqMessage(redisRecord, MessageType.MT_UNKNOWN);
        }
    }

    public void test(Record record) {
        messageSendThreadPool.submit(new StateHandler(record));
    }

    /**
     * 发送发票数据同步请求消息
     */
    private void sendReqMessage(Record record, MessageType messageType) {
        MessageWrap messageReq = MessageUtil.buildMessageReq(record, messageType);
        reqProducer.sendMessageAsync(Long.toString(record.getId()), messageReq.toByteArray());
        printMessageOutLog(reqProducer.getTopic(), Long.toString(record.getId()));
    }

    /**
     * 停止读取新任务
     */
    @Override
    protected void stopReadNewTask() {
        stopServiceRequested = true;
    }
}
