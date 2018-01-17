package com.py.modular.second.service;

import com.py.modular.common.base.*;
import com.py.modular.common.base.KafkaMessage.MessageType;
import com.py.modular.common.base.KafkaMessage.MessageWrap;
import com.py.modular.common.base.config.ServiceConfig;
import com.py.modular.common.base.constants.KafkaConstants;
import com.py.modular.common.base.constants.ServiceFlagConstants;
import com.py.modular.common.base.constants.TopicName;
import com.py.modular.common.base.utils.ExceptionStackUtil;
import com.py.modular.common.base.utils.MessageUtil;
import com.py.modular.common.database.dao.RecordMapper;
import com.py.modular.common.database.entity.Record;
import com.py.modular.common.kafka.MessageConsumer;
import com.py.modular.common.kafka.MessageProducer;
import com.py.modular.common.redis.dao.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * 状态更新服务
 *
 * @author PYSASUKE
 */
@Service
public class SecondService extends BaseService {

    /**
     * 请求暂停服务
     */
    private volatile boolean stopServiceRequested = false;

    /**
     * 服务标识
     */
    private static final String SERVICE_ID_FLAG = ServiceFlagConstants.SERVICE_FLAG_KAFKA_SECOND;

    /**
     * 消息队列consumer
     */
    private MessageConsumer messageConsumer;

    /**
     * 消息处理线程数
     */
    private final int messageThreadNumber = KafkaConstants.MESSAGE_THREAD_NUMBER;

    /**
     * 消息队列
     */
    private ArrayBlockingQueue[] messageQueue;
    /**
     * 应答队列producer
     */
    private MessageProducer reqProducer;
    private final RedisService redisService;
    private final RecordMapper recordMapper;

    @Autowired
    public SecondService(RedisService redisService,
                         RecordMapper recordMapper) {
        this.redisService = redisService;
        this.recordMapper = recordMapper;
        ServiceConfig serviceConfig = ServiceConfig.builder()
                .serviceIdFlag(SERVICE_ID_FLAG)
                .bootstrapServers(KafkaConstants.BOOTSTRAP_SERVERS)
                .build();
        setServiceConfig(serviceConfig);

        reqProducer = createProducer(TopicName.KAFKA_SECOND, "second");
        messageConsumer = createConsumer(Arrays.asList(TopicName.KAFKA_FIRST,
                "test"), null);
        messageConsumer.start();

        startService();
    }

    /**
     * 启动服务
     */
    @Override
    public void startService() {
        messageQueue = new ArrayBlockingQueue[messageThreadNumber];
        for (int i = 0; i < messageThreadNumber; i++) {
            messageQueue[i] = new ArrayBlockingQueue(1);
            Thread thread = new Thread(new StateHandler(i));
            thread.setName("Thread-state_handler-" + i);
            thread.start();
        }
    }

    /**
     * 收到消息回调
     */
    @Override
    @SuppressWarnings(value = {"unchecked"})
    public void onMessage(String key, byte[] value, String topic, int partition) {
        try {
            int mod = Math.abs(key.hashCode() % messageThreadNumber);
            messageQueue[mod].put(value);
        } catch (Exception e) {
            logger.error(ExceptionStackUtil.getExceptionStack(e));
        }
    }

    @Override
    public void onSuccess(MessageProducer producer, String key, byte[] value, int partition, long offset) {

    }

    @Override
    public void onFailure(MessageProducer producer, String key, byte[] value, Exception e) {

    }

    /**
     * 状态处理
     */
    private class StateHandler implements Runnable {
        private int index;

        StateHandler(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            while (!stopServiceRequested) {
                try {
                    MessageWrap wrap = MessageWrap.parseFrom((byte[]) messageQueue[index].take());
                    if (wrap == null) {
                        printMessageInErrorLog("", "错误的消息格式");
                        continue;
                    }
                    printMessageInLog(Long.toString(wrap.getMessageReq().getId()));
                    if (wrap.getMessageType() == MessageType.MT_FIRST) {
                        Record redisRecord = (Record) redisService.get("record:" + Long.toString(wrap.getMessageReq().getId()));
                        if (null == redisRecord || 1 != redisRecord.getFirstState() || 0 != redisRecord.getSecondState()) {
                            continue;
                        }
                        recordMapper.updateSecondState(wrap.getMessageReq().getId());
                        Record record = recordMapper.selectById(wrap.getMessageReq().getId());
                        redisService.set("record:" + Long.toString(record.getId()), record);
                        sendReqMessage(record, MessageType.MT_SECOND);
                        System.out.println("second service complete");
                    }
                } catch (Exception e) {
                    logger.error(ExceptionStackUtil.getExceptionStack(e));
                }
            }
        }
    }

    /**
     * 停止读取新任务
     */
    @Override
    protected void stopReadNewTask() {
        messageConsumer.close();
        stopServiceRequested = true;
    }

    /**
     * 发送发票数据同步请求消息
     */
    private void sendReqMessage(Record record, MessageType messageType) {
        MessageWrap messageReq = MessageUtil.buildMessageReq(record, messageType);
        reqProducer.sendMessageAsync(Long.toString(record.getId()), messageReq.toByteArray());
        printMessageOutLog(reqProducer.getTopic(),Long.toString(record.getId()));
    }

}
