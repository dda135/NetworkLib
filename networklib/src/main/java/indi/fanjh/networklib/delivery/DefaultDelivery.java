package indi.fanjh.networklib.delivery;


import indi.fanjh.networklib.Request;

/**
* @author fanjh
* @date 2018/3/22 11:01
* @description 默认的投递者
* @note
**/
public class DefaultDelivery<T> implements IDelivery<T>{
    private boolean callAtBackground;

    public void setCallAtBackground(boolean callAtBackground) {
        this.callAtBackground = callAtBackground;
    }

    @Override
    public void deliverySuccessObject(final Request<T> request, final T object, final int from) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                request.getSuccessListener().onCall(object, from);
            }
        };
        if(callAtBackground){
            BackgroundDelivery.EXECUTOR.execute(runnable);
        }else{
            MainDelivery.MAIN_HANDLER.post(runnable);
        }
    }

    @Override
    public void deliveryError(final Request<T> request, final Throwable ex, final int from) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                request.getErrorListener().onCall(ex, from);
            }
        };
        if(callAtBackground){
            BackgroundDelivery.EXECUTOR.execute(runnable);
        }else{
            MainDelivery.MAIN_HANDLER.post(runnable);
        }
    }
}
