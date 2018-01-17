package com.py.modular.common.base.utils;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期处理工具类
 *
 * @author PYSASUKE
 */
public class DateUtil extends DateUtils {

    /**
     * 根据Date类型日期获取String类型日期
     *
     * @param date    Date类型日期
     * @param pattern 格式
     * @return 若传入的格式正确，返回String类型的日期；否则，返回null
     */
    public static String getStringByPattern(Date date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    /**
     * 获取距离当前N小时的时间
     *
     * @param amount
     * @return
     */
    public static Date getBeforeHour(int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, -amount);
        return calendar.getTime();
    }

    /**
     * 获取距离当前N天的时间
     *
     * @param amount
     * @return
     */
    public static Date getBeforeDay(int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -amount);
        return calendar.getTime();
    }
}
