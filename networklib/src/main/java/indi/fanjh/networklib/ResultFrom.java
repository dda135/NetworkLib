package indi.fanjh.networklib;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
* @author fanjh
* @date 2018/3/22 9:43
* @description 结果来源定义
* @note 用于区分数据的来源
**/
@Retention(RetentionPolicy.SOURCE)
@IntDef({ResultFrom.FROM_NETWORK, ResultFrom.FROM_DISK_CACHE})
public @interface ResultFrom {
    /**
     * 结果来源于网络请求
     */
    public static final int FROM_NETWORK = 1;
    /**
     * 结果来源于硬盘缓存，相对来说比较常用
      */
    public static final int FROM_DISK_CACHE = 2;

}
