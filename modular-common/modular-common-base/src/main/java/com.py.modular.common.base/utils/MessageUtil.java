package com.py.modular.common.base.utils;

import com.py.modular.common.base.KafkaMessage.MessageDB;
import com.py.modular.common.base.KafkaMessage.MessageType;
import com.py.modular.common.base.KafkaMessage.MessageWrap;
import com.py.modular.common.database.entity.Record;

/**
 * 消息转换工具类
 *
 * @author PYSASUKE
 */
public class MessageUtil {

    /**
     * 构建请求消息
     *
     * @param record
     * @param messageType
     * @return
     */
    public static MessageWrap buildMessageReq(Record record, MessageType messageType) {
        MessageDB messageDB = reqToMessage(record);

        return MessageWrap.newBuilder()
                .setMessageType(messageType)
                .setMessageReq(messageDB)
                .build();
    }

    private static MessageDB reqToMessage(Record record) {
        return MessageDB.newBuilder()
                .setId(record.getId())
                .setDescribe(record.getDescribe())
                .setCreateTime(DateUtil.getStringByPattern(record.getCreateTime(), "yyyy-MM-dd HH:mm:ss"))
                .setUpdateTime(DateUtil.getStringByPattern(record.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"))
                .setFirstState(record.getFirstState())
                .setSecondState(record.getSecondState())
                .setThirdState(record.getThirdState())
                .setFirstTime(null == record.getFirstTime() ? "" : DateUtil.getStringByPattern(record.getFirstTime(), "yyyy-MM-dd HH:mm:ss"))
                .setSecondTime(null == record.getSecondTime() ? "" : DateUtil.getStringByPattern(record.getSecondTime(), "yyyy-MM-dd HH:mm:ss"))
                .setThirdTime(null == record.getThirdTime() ? "" : DateUtil.getStringByPattern(record.getThirdTime(), "yyyy-MM-dd HH:mm:ss"))
                .build();
    }

}

