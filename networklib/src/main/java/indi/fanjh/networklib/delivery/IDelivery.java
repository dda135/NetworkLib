package indi.fanjh.networklib.delivery;

import indi.fanjh.networklib.Request;
import indi.fanjh.networklib.ResultFrom;

/**
* @author fanjh
* @date 2018/3/22 9:38
* @description 结果投递
* @note 主要是影响结果所执行的线程
**/
public interface IDelivery<T> {
    /**
     * 发送请求成功后的对象
     * @param request 当次请求
     * @param object 需要发送的对象
     * @param from 当次请求的来源
     */
    void deliverySuccessObject(Request<T> request, T object, @ResultFrom int from);

    /**
     * 发送当次请求失败
     * @param request 当次请求
     * @param ex 失败原因
     * @param from 当次请求的来源
     */
    void deliveryError(Request<T> request, Throwable ex, @ResultFrom int from);

}
