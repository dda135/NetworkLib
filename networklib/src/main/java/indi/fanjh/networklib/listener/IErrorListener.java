package indi.fanjh.networklib.listener;

import indi.fanjh.networklib.ResultFrom;

/**
* @author fanjh
* @date 2018/3/21 19:06
* @description 请求中出现异常后进行结果回调
* @note
**/
public interface IErrorListener {
    /**
     * 请求中出现异常后进行回调
     * @param exception 请求失败的原因
     * @param from 当次异常的来源
     */
    void onCall(Throwable exception, @ResultFrom int from);

}
