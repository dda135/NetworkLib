package indi.fanjh.networklib;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
* @author fanjh
* @date 2018/3/21 11:07
* @description 请求策略
* @note
**/
@Retention(RetentionPolicy.SOURCE)
@IntDef({RequestStrategy.COMMON, RequestStrategy.ONLY_PUT_CACHE, RequestStrategy.ONLY_GET_CACHE, RequestStrategy.FIRST_CACHE_NEXT_NETWORK, RequestStrategy.COMMON_CACHE})
public @interface RequestStrategy {
    /**
     * 直接发请求，请求完进行成功/失败回调
     */
    public static final int COMMON = 0;
    /**
     * 直接发起请求，请求完成后直接存回缓存中，但是不进行任何回调
     */
    public static final int ONLY_PUT_CACHE = 1;
    /**
     * 只从缓存中获取数据，然后进行成功/失败回调
     */
    public static final int ONLY_GET_CACHE = 2;
    /**
     * 先从缓存中获取数据，如果成功进行成功回调，失败无操作，然后接着发起网络请求，请求完成后进行成功/失败回调
     */
    public static final int FIRST_CACHE_NEXT_NETWORK = 3;
    /**
     * 先从缓存中获取数据，如果成功则进行成功回调，失败则发起请求，并且在请求成功后会尝试存回缓存，请求完成正常进行成功/失败回调
     */
    public static final int COMMON_CACHE = 4;

}
