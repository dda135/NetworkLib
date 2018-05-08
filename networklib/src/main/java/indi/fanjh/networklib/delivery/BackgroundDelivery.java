package indi.fanjh.networklib.delivery;

import android.support.annotation.NonNull;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import indi.fanjh.networklib.Request;


/**
* @author fanjh
* @date 2018/3/22 10:22
* @description 将回调投递到工作线程中
* @note 为了避免阻塞网络请求的线程，从而导致无法快速的进行下一个等待中的请求，故而此处采用的是调度到新的线程中处理的方式
**/
public class BackgroundDelivery<T> implements IDelivery<T> {
    public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setName("BackgroundDelivery");
            return thread;
        }
    });

    @Override
    public void deliverySuccessObject(final Request<T> request, final T object, final int from) {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                request.getSuccessListener().onCall(object, from);
            }
        });
    }

    @Override
    public void deliveryError(final Request<T> request, final Throwable ex, final int from) {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                request.getErrorListener().onCall(ex, from);
            }
        });
    }
}
