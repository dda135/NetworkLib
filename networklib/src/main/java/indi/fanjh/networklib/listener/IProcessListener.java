package indi.fanjh.networklib.listener;

/**
* @author fanjh
* @date 2018/3/23 14:29
* @description 一次请求启动和结束的监听
* @note 注意所在线程最终都会在主线程中
**/
public interface IProcessListener {
    /**
     * 请求开始
     */
    void onStart();

    /**
     * 请求结束
     */
    void onEnd();

}
