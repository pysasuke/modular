package com.py.modular.common.mongo.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;


/**
 * Mongo对象
 * 需要与mongo中对象(文档)名一致
 *
 * @author PYSASUKE
 */
@Data
public class MgRecord {
    //插入mongo时会自动生成_id，如果不加这个字段则会把id属性当成_id
    @Id
    private ObjectId objectId;
    @Field
    private Long id;
    private String describe;
    private Date createTime;
    private Date updateTime;
    private int firstState;
    private int secondState;
    private int thirdState;
    private Date firstTime;
    private Date secondTime;
    private Date thirdTime;
}
