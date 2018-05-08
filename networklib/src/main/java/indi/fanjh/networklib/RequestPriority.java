package indi.fanjh.networklib;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
* @author fanjh
* @date 2018/3/22 14:02
* @description 请求优先级
* @note 为了避免过度滥用优先级，统一几个等级
**/
@Retention(RetentionPolicy.SOURCE)
@IntDef({RequestPriority.LOW, RequestPriority.NORMAL, RequestPriority.HIGH})
public @interface RequestPriority {
    public static final int LOW = 0;
    public static final int NORMAL = 5;
    public static final int HIGH = 10;
}
