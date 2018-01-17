package com.py.modular.common.base.constants;

/**
 * 重做相关常量
 *
 * @author PYSASUKE
 * @date 2018/1/15
 */
public class RedoConstants {
    /**
     * 重做每批次大小
     */
    public static final int REDO_BATCH_SIZE = 100;
    /**
     * 遗留数据迁移检测周期(小时)
     */
    public static final int SYNC_TO_MONGO_PERIOD = 2;

    /**
     * 遗留数据重推周期(分钟)
     */
    public static final int REDO_PERIOD = 10;

    /**
     * 只处理N小时之前的错误数据
     */
    public static final int RENEW_BEFORE_TIME = 6;
}
