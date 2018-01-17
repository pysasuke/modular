package com.py.modular.common.mongo;

import com.py.modular.common.mongo.dao.MongoRecordRepository;
import com.py.modular.common.mongo.entity.MgRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * mongodb测试
 *
 * @author pysasuke
 * @create 2017-09-15
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MongoTest {
    @Autowired
    private MongoRecordRepository mongoRecordRepository;

    @Test
    public void test() {
        MgRecord mgRecord = new MgRecord();
        mgRecord.setId(1L);
        mgRecord.setDescribe("pysasuke");
        mgRecord.setFirstState(1);
        mgRecord.setSecondState(1);
        mgRecord.setThirdState(1);
        mgRecord.setFirstTime(new Date());
        mgRecord.setSecondTime(new Date());
        mgRecord.setThirdTime(new Date());
        //插入成功后_id属性有值(mongo数据库生成的)
        mongoRecordRepository.insert(mgRecord);
        //该方法使用的是mongodb中ObjectId为条件的
//        MgUser selectMgUser = userRepository.findOne(1L);
//        MgUser selectMgUser = userRepository.findByNickname("pysasuke");
        MgRecord selectMgRecord = mongoRecordRepository.findById(1L);
        Assert.assertEquals(mgRecord.getId(), selectMgRecord.getId());
    }
    @Test
    public void test2(){
        MgRecord mgRecord1 = mongoRecordRepository.findById(30L);
        System.out.println(mgRecord1);
    }
}
