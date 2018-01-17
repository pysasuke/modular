package com.py.modular.common.mongo.dao;

import com.py.modular.common.mongo.entity.MgRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * mongo数据层操作类
 *
 * @author PYSASUKE
 */
@Repository
public interface MongoRecordRepository extends MongoRepository<MgRecord, Long> {

    /**
     * 根据id为条件搜索(默认实现的findOne方法是以mongodb中ObjectId为条件的)
     * 默认实现类似findBy*(支持*And*)方法，其中*号为实体属性名
     *
     * @param id
     * @return
     */
    MgRecord findById(Long id);
}
