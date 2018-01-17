package com.py.modular.common.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

/**
 * 消息消费实现类
 *
 * @author PYSASUKE
 */
public class MessageConsumerImpl implements MessageConsumer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private MessageConsumerConfig consumerConfig;
    private final KafkaConsumer<String, byte[]> consumer;

    /**
     * 请求关闭consumer
     */
    private volatile boolean closeRequested = false;

    public MessageConsumerImpl(MessageConsumerConfig config) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, config.getClientId());
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.getMaxPollRecords());
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        //将参数enable.auto.commit设置成false，即关闭了offset的自动提交功能，以防止出现当处理消息的时间过长时，报自动提交失败的错误
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        //当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费(consumer第一次启动时候,在zookeeper中没有初始的offset)
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        this.consumerConfig = config;
        this.consumer = new KafkaConsumer<>(properties);
    }

    @Override
    public void start() {
        Thread thread = new Thread(new ConsumerThread());
        thread.setName("Thread-" + consumerConfig.getClientId());
        thread.start();
    }

    @Override
    public void close() {
        closeRequested = true;
    }

    /**
     * 从kafka消费消息的线程
     */
    private class ConsumerThread extends Thread {
        @Override
        public void run() {
            consumer.subscribe(consumerConfig.getTopics());
            MessageConsumerCallback callback = consumerConfig.getConsumerCallback();
            ConsumerRecords<String, byte[]> records;

            while (!closeRequested) {
                try {
                    records = consumer.poll(1000);
                    if (records.isEmpty()) {
                        continue;
                    }

                    // 提前提交，防止消息处理时间超过session超时时间，导致提交失败。
                    // 消息完整性由业务系统提供保证。
                    consumer.commitSync();
                } catch (Exception e) {
                    logger.error(getExceptionStack(e));
                    continue;
                }

                for (ConsumerRecord<String, byte[]> record : records) {
                    try {
                        callback.onMessage(record.key(), record.value(),
                                record.topic(), record.partition());
                    } catch (Exception e) {
                        logger.error(getExceptionStack(e));
                    }
                }
            }
            consumer.close();
        }
    }

    public static String getExceptionStack(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
