package com.py.modular.common.database;

import com.py.modular.common.database.dao.RecordMapper;
import com.py.modular.common.database.entity.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * mybatis操作mysql对象增删改查测试
 *
 * @author PYSASUKE
 * @create 2017-09-15
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MybatisTest {

    @Autowired
    private RecordMapper recordMapper;

    @Test
    public void testInsert() {
        Record record = generateRecord();
        int row = recordMapper.insert(record);
        Assert.assertEquals(1, row);
    }

    @Test
    public void testSelect() {
        Record record = generateRecord();
        /*
        插入成功后id会有值(mysql数据库生成的)
        前提是：
        1.主键设置为自动增长
        2.xml中insert语句设置有:useGeneratedKeys="true" keyProperty="id"
         */
        int row = recordMapper.insert(record);
        Assert.assertEquals(1, row);

        Record selectedRecord = recordMapper.selectById(record.getId());
        Assert.assertNotNull(selectedRecord);
        Assert.assertEquals(record.getId(), selectedRecord.getId());
    }

    @Test
    public void testUpdate() {
        Record record = generateRecord();
        int row = recordMapper.insert(record);
        Assert.assertEquals(1, row);

        Record selectedRecord = recordMapper.selectById(record.getId());
        Assert.assertNotNull(selectedRecord);
        selectedRecord.setDescribe("我是");
        recordMapper.updateById(selectedRecord);

        Record updatedRecord = recordMapper.selectById(selectedRecord.getId());
        Assert.assertNotNull(updatedRecord);
        Assert.assertEquals(selectedRecord.getDescribe(), updatedRecord.getDescribe());
    }

    @Test
    public void testDelete() {
        Record record = generateRecord();
        int row = recordMapper.insert(record);
        Assert.assertEquals(1, row);

        recordMapper.deleteById(record.getId());
        Record selectedRecord = recordMapper.selectById(record.getId());
        Assert.assertNull(selectedRecord);
    }

    private Record generateRecord() {
        Record record = new Record();
        record.setDescribe("test");
        return record;
    }

    @Test
    public void testRecord() {
        Record record = generateRecord();
        recordMapper.insert(record);
    }
}
