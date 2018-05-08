package indi.fanjh.networklib.delivery;

import android.os.Handler;
import android.os.Looper;

import indi.fanjh.networklib.Request;


/**
* @author fanjh
* @date 2018/3/22 10:22
* @description 将回调投递到主线程中
* @note
**/
public class MainDelivery<T> implements IDelivery<T> {
    public static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    @Override
    public void deliverySuccessObject(final Request<T> request, final T object, final int from) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                request.getSuccessListener().onCall(object, from);
            }
        });
    }

    @Override
    public void deliveryError(final Request<T> request, final Throwable ex, final int from) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                request.getErrorListener().onCall(ex, from);
            }
        });
    }
}
