package com.py.modular.exhandler.service;

import com.py.modular.common.base.BaseService;
import com.py.modular.common.base.KafkaMessage.MessageType;
import com.py.modular.common.base.KafkaMessage.MessageWrap;
import com.py.modular.common.base.config.ServiceConfig;
import com.py.modular.common.base.constants.KafkaConstants;
import com.py.modular.common.base.constants.RedoConstants;
import com.py.modular.common.base.constants.TopicName;
import com.py.modular.common.base.enums.ServiceEnum;
import com.py.modular.common.base.enums.StateEnum;
import com.py.modular.common.base.utils.DateUtil;
import com.py.modular.common.base.utils.ExceptionStackUtil;
import com.py.modular.common.base.utils.MessageUtil;
import com.py.modular.common.database.dao.RecordMapper;
import com.py.modular.common.database.entity.Record;
import com.py.modular.common.kafka.MessageProducer;
import com.py.modular.common.mongo.dao.MongoRecordRepository;
import com.py.modular.common.mongo.entity.MgRecord;
import com.py.modular.common.redis.dao.RedisService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 异常数据处理服务
 *
 * 自动处理的数据对象：
 * 1.未完成所有所有操作(三个服务)的数据
 * 2.完成所有操作但未转移到MongoDB的数据
 * 3.开票速率过快造成的错误，按内部错误处理，状态值设置为3。
 *
 * 相关查询策略常量在RedoConstants类中
 * @author PYSASUKE
 */
@Service
public class ExService extends BaseService {
    /**
     * 服务标识
     */
    private static final String SERVICE_ID_FLAG = "modular-exhandler";

    /**
     * 停止服务
     */
    private volatile boolean stopServiceRequested = false;

    /**
     * 空列表
     */
    private static List<Record> zeroList = Collections.emptyList();

    private final RedisService redisService;
    private final MongoRecordRepository mongoRecordRepository;
    private final RecordMapper recordMapper;

    @Autowired
    public ExService(RedisService redisService,
                     MongoRecordRepository mongoRecordRepository,
                     RecordMapper recordMapper) {

        this.mongoRecordRepository = mongoRecordRepository;
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
        //处理first、second、third操作都未完成的数据
        new Thread(new FailedStateProcessThread(ServiceEnum.ZERO)).start();
        //处理完成first但未完成second、third操作的数据
        new Thread(new FailedStateProcessThread(ServiceEnum.FIRST)).start();
        //处理完成first、second但未完成third操作的数据
        new Thread(new FailedStateProcessThread(ServiceEnum.SECOND)).start();
        //处理可转移的到MongoDB但未转移的数据
        new Thread(new SyncReqThread()).start();
    }

    /**
     * 获取操作失败记录
     */
    private List<Record> getInvoices(ServiceEnum serviceEnum) {
        List<Record> list = null;
        Date createTime = DateUtil.getBeforeHour(RedoConstants.RENEW_BEFORE_TIME);
        switch (serviceEnum) {
            case ZERO:
                list = recordMapper.findByStates(StateEnum.UNDO.code, StateEnum.UNDO.code, StateEnum.UNDO.code, createTime, RedoConstants.REDO_BATCH_SIZE);
                break;
            case FIRST:
                list = recordMapper.findByStates(StateEnum.SUCCESS.code, StateEnum.UNDO.code, StateEnum.UNDO.code, createTime, RedoConstants.REDO_BATCH_SIZE);
                break;
            case SECOND:
                list = recordMapper.findByStates(StateEnum.SUCCESS.code, StateEnum.SUCCESS.code, StateEnum.UNDO.code, createTime, RedoConstants.REDO_BATCH_SIZE);
                break;
            case THIRD:
                list = recordMapper.findByStates(StateEnum.SUCCESS.code, StateEnum.SUCCESS.code, StateEnum.SUCCESS.code, createTime, RedoConstants.REDO_BATCH_SIZE);
                break;
            default:
                break;
        }
        return list == null ? zeroList : list;
    }


    @Override
    public void onMessage(String key, byte[] value, String topic, int partition) {

    }

    @Override
    public void onSuccess(MessageProducer producer, String key, byte[] value, int partition, long offset) {

    }

    @Override
    public void onFailure(MessageProducer producer, String key, byte[] value, Exception e) {

    }

    /**
     * 流程失败处理线程
     */
    private class FailedStateProcessThread implements Runnable {

        /**
         * 状态类型
         */
        private ServiceEnum serviceEnum;

        /**
         * 日志标识
         */
        private String loggerId;

        /**
         * topic名称
         */
        private String topicName;

        /**
         * 消息队列producer
         */
        private MessageProducer producer = null;

        FailedStateProcessThread(ServiceEnum serviceEnum) {
            this.serviceEnum = serviceEnum;
            String clientIdSuffix;

            if (isZero(serviceEnum)) {
                loggerId = "[REDO ZERO]:";
                clientIdSuffix = "zero";
                topicName = TopicName.KAFKA_ZERO;
            } else if (isFirst(serviceEnum)) {
                loggerId = "[REDO FIRST]:";
                clientIdSuffix = "first";
                topicName = TopicName.KAFKA_FIRST;
            } else if (isSecond(serviceEnum)) {
                loggerId = "[REDO SECOND]:";
                clientIdSuffix = "second";
                topicName = TopicName.KAFKA_SECOND;
            } else {
                return;
            }

            producer = createProducer(topicName, clientIdSuffix);
        }

        private boolean isZero(ServiceEnum serviceEnum) {
            return ServiceEnum.ZERO == serviceEnum;
        }

        private boolean isFirst(ServiceEnum serviceEnum) {
            return ServiceEnum.FIRST == serviceEnum;
        }

        private boolean isSecond(ServiceEnum serviceEnum) {
            return ServiceEnum.SECOND == serviceEnum;
        }

        @Override
        public void run() {
            List<Record> recordList;
            while (!stopServiceRequested) {
                try {
                    recordList = getInvoices(serviceEnum);
                    logger.info(loggerId + recordList.size());
                    for (Record record : recordList) {
                        try {
                            sendMessage(record);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error(ExceptionStackUtil.getExceptionStack(e));
                        }
                    }
                    recordList.clear();
                    TimeUnit.MINUTES.sleep(RedoConstants.REDO_PERIOD);
                } catch (Exception e) {
                    logger.error(ExceptionStackUtil.getExceptionStack(e));
                }
            }
        }

        private boolean sendMessage(Record record) {
            if (producer == null) {
                return false;
            }

            MessageWrap messageWrap;
            if (isZero(serviceEnum)) {
                messageWrap = MessageUtil.buildMessageReq(record, MessageType.MT_UNKNOWN);
            } else if (isFirst(serviceEnum)) {
                messageWrap = MessageUtil.buildMessageReq(record, MessageType.MT_FIRST);
            } else if (isSecond(serviceEnum)) {
                messageWrap = MessageUtil.buildMessageReq(record, MessageType.MT_SECOND);
            } else {
                return false;
            }
            if (messageWrap == null) {
                printMessageOutErrorLog(topicName, Long.toString(record.getId()), "组装消息出错");
                return false;
            }

            try {
                producer.sendMessageSync(Long.toString(record.getId()), messageWrap.toByteArray());
                printMessageOutLog(topicName, Long.toString(record.getId()));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(ExceptionStackUtil.getExceptionStack(e));
                return false;
            }
        }
    }

    private class SyncReqThread implements Runnable {
        @Override
        public void run() {
            while (!stopServiceRequested) {
                Date createTime = DateUtil.getBeforeDay(RedoConstants.RENEW_BEFORE_TIME);
                List<Record> recordList = recordMapper.findCanSync(createTime, RedoConstants.REDO_BATCH_SIZE);
                if (!CollectionUtils.isEmpty(recordList)) {
                    for (Record record : recordList) {
                        // 同步到mongo并删除数据库中数据
                        MgRecord mgRecord = new MgRecord();
                        BeanUtils.copyProperties(record, mgRecord);
                        mongoRecordRepository.insert(mgRecord);
                        redisService.remove("record:" + Long.toString(record.getId()));
                        recordMapper.deleteById(record.getId());
                    }

                    if (recordList.size() == RedoConstants.REDO_BATCH_SIZE) {
                        continue;
                    }
                }

                try {
                    TimeUnit.HOURS.sleep(RedoConstants.SYNC_TO_MONGO_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    logger.error(ExceptionStackUtil.getExceptionStack(e));
                }
            }
        }
    }

    /**
     * 停止读取新任务
     */
    @Override
    public void stopReadNewTask() {
        stopServiceRequested = true;
    }
}
