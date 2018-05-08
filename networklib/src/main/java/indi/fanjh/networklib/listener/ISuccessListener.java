package indi.fanjh.networklib.listener;


import indi.fanjh.networklib.ResultFrom;

/**
* @author fanjh
* @date 2018/3/21 19:06
* @description 请求成功后进行结果回调
* @note
**/
public interface ISuccessListener<T> {
    /**
     * 请求成功进行回调
     * @param object 请求成功后获取的对象
     * @param from 对象的来源
     */
    void onCall(T object, @ResultFrom int from);

}
