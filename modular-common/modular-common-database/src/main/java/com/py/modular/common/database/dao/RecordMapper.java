package com.py.modular.common.database.dao;


import com.py.modular.common.database.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 数据库Mybatis操作类
 *
 * @author PYSASUKE
 */
@Mapper
public interface RecordMapper {
    /**
     * 根据id删除记录
     *
     * @param id
     * @return
     */
    int deleteById(@Param("id") long id);

    /**
     * 插入记录
     *
     * @param record
     * @return
     */
    int insert(Record record);

    /**
     * 根据id查询记录
     *
     * @param id
     * @return
     */
    Record selectById(@Param("id") long id);

    /**
     * 根据id修改记录
     *
     * @param record
     * @return
     */
    int updateById(Record record);

    /**
     * 修改相关字段值
     *
     * @param id
     */
    void updateFirstState(@Param("id") long id);

    /**
     * 修改相关字段值
     *
     * @param id
     */
    void updateSecondState(@Param("id") long id);

    /**
     * 修改相关字段值
     *
     * @param id
     */
    void updateThirdState(@Param("id") long id);

    /**
     * 查询可同步到mongo中的记录
     *
     * @param createTime
     * @param limit
     * @return
     */
    List<Record> findCanSync(@Param("createTime") Date createTime, @Param("limit") int limit);

    /**
     * 修改状态
     *
     * @param firstState
     * @param secondState
     * @param thirdState
     * @param createTime
     * @param limit
     * @return
     */
    List<Record> findByStates(@Param("firstState") int firstState,
                              @Param("secondState") int secondState,
                              @Param("thirdState") int thirdState,
                              @Param("createTime") Date createTime,
                              @Param("limit") int limit);

}