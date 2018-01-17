package com.py.modular.third.service;

import com.py.modular.common.base.BaseService;
import com.py.modular.common.base.KafkaMessage.MessageType;
import com.py.modular.common.base.KafkaMessage.MessageWrap;
import com.py.modular.common.base.config.ServiceConfig;
import com.py.modular.common.base.constants.KafkaConstants;
import com.py.modular.common.base.constants.ServiceFlagConstants;
import com.py.modular.common.base.constants.TopicName;
import com.py.modular.common.base.utils.ExceptionStackUtil;
import com.py.modular.common.database.dao.RecordMapper;
import com.py.modular.common.database.entity.Record;
import com.py.modular.common.kafka.MessageConsumer;
import com.py.modular.common.kafka.MessageProducer;
import com.py.modular.common.mongo.dao.MongoRecordRepository;
import com.py.modular.common.mongo.entity.MgRecord;
import com.py.modular.common.redis.dao.RedisService;
import org.springframework.beans.BeanUtils;
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
public class ThirdService extends BaseService {

    /**
     * 请求暂停服务
     */
    private volatile boolean stopServiceRequested = false;

    /**
     * 服务标识
     */
    private static final String SERVICE_ID_FLAG = ServiceFlagConstants.SERVICE_FLAG_KAFKA_THIRD;

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

    private final RedisService redisService;
    private final RecordMapper recordMapper;
    private final MongoRecordRepository mongoRecordRepository;

    @Autowired
    public ThirdService(RedisService redisService,
                        RecordMapper recordMapper,
                        MongoRecordRepository mongoRecordRepository) {
        this.redisService = redisService;
        this.recordMapper = recordMapper;
        this.mongoRecordRepository = mongoRecordRepository;
        ServiceConfig serviceConfig = ServiceConfig.builder()
                .serviceIdFlag(SERVICE_ID_FLAG)
                .bootstrapServers(KafkaConstants.BOOTSTRAP_SERVERS)
                .build();
        setServiceConfig(serviceConfig);

        messageConsumer = createConsumer(Arrays.asList(TopicName.KAFKA_SECOND,
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

                    if (wrap.getMessageType() == MessageType.MT_SECOND) {
                        Record redisRecord = (Record) redisService.get("record:" + Long.toString(wrap.getMessageReq().getId()));
                        if (null == redisRecord || 1 != redisRecord.getSecondState() || 0 != redisRecord.getThirdState()) {
                            continue;
                        }
                        recordMapper.updateThirdState(wrap.getMessageReq().getId());
                        Record record = recordMapper.selectById(wrap.getMessageReq().getId());
                        redisService.set("record:" + Long.toString(record.getId()), record);

                        if (record.getFirstState() == 1
                                && record.getSecondState() == 1
                                && record.getThirdState() == 1) {
                            syncToMongo(record);
                            System.out.println("third service complete");
                        }
                    }
                } catch (Exception e) {
                    logger.error(ExceptionStackUtil.getExceptionStack(e));
                    e.printStackTrace();
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


    private void syncToMongo(Record record) {
        MgRecord mgRecord = new MgRecord();
        BeanUtils.copyProperties(record, mgRecord);
        mongoRecordRepository.insert(mgRecord);
        redisService.remove("record:" + Long.toString(record.getId()));
        recordMapper.deleteById(record.getId());
    }
}
