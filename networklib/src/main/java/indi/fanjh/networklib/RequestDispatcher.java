package indi.fanjh.networklib;

import android.support.annotation.NonNull;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import indi.fanjh.networklib.cache.ICache;


/**
* @author fanjh
* @date 2018/3/22 14:14
* @description 请求行为分发者
* @note 针对不同的请求策略进行操作
**/
abstract class RequestDispatcher<T> {

    static final ThreadPoolExecutor CACHE_EXECUTOR = new ThreadPoolExecutor(0, 128, 60, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new ThreadFactory() {
        private AtomicInteger atomicInteger = new AtomicInteger(0);
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setName("CACHE_EXECUTOR_"+atomicInteger.decrementAndGet());
            return thread;
        }
    });

    /**
     * 启动拥有缓存策略的请求
     * 所在线程为请求开始的线程，可能是主或者工作线程
     * @param request 当前请求
     * @param cache 当前请求所用到的缓存
     */
    abstract void start(Request<T> request,ICache<T> cache);

    /**
     * 请求成功之后的回调
     * 默认在子线程中
     * @param request 当前请求
     * @param cache 当前请求所用到的缓存
     * @param object 当前请求成功的对象
     * @param cacheTime 当前要求缓存的时长，单位s
     */
    abstract void successEnd(Request<T> request, ICache<T> cache, T object, int cacheTime);

    /**
     * 请求失败之后的回调
     * 默认在子线程中
     * @param request 当前请求
     * @param ex 当前请求出现的异常
     */
    abstract void errorEnd(Request<T> request,Throwable ex);

}
