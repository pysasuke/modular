package com.py.modular.common.database.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 记录实体类
 *
 * @author PYSASUKE
 */
@Data
public class Record implements Serializable {
    private static final long serialVersionUID = 5099607145367813828L;
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