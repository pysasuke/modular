package com.py.modular.common.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * 消息生成者实现类
 *
 * @author PYSASUKE
 */
public class MessageProducerImpl implements MessageProducer {
    private MessageProducerConfig producerConfig;
    private String topic;
    private MessageProducer thisProducer;
    private final Producer producer;

    public MessageProducerImpl(MessageProducerConfig producerConfig) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerConfig.getBootstrapServers());
        if (producerConfig.getClientId() != null) {
            String producerId = producerConfig.getClientId().trim();
            if (!producerId.isEmpty()) {
                properties.put(ProducerConfig.CLIENT_ID_CONFIG, producerId);
            }
        }
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 0);
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        this.thisProducer = this;
        this.producerConfig = producerConfig;
        this.topic = producerConfig.getTopic();
        this.producer = new KafkaProducer<>(properties);
    }

    @Override
    @SuppressWarnings(value = {"unchecked"})
    public void sendMessageAsync(final String key, final byte[] value) {
        producer.send(new ProducerRecord<>(topic, key, value), new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (metadata != null) {
                    producerConfig.getProducerCallback().onSuccess(thisProducer,
                            key, value, metadata.partition(), metadata.offset());
                } else {
                    producerConfig.getProducerCallback().onFailure(thisProducer,
                            key, value, exception);
                }
            }
        });
    }

    @Override
    @SuppressWarnings(value = {"unchecked"})
    public void sendMessageSync(String key, byte[] value)
            throws InterruptedException, ExecutionException {
        producer.send(new ProducerRecord<>(topic, key, value)).get();
    }

    @Override
    public void close() {
        producer.close();
    }

    @Override
    public MessageProducerConfig getConfig() {
        return producerConfig;
    }

    @Override
    public String getTopic() {
        return topic;
    }
}
