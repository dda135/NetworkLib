package indi.fanjh.networklib.worker;


import java.io.InputStream;

import indi.fanjh.networklib.Request;


/**
* @author fanjh
* @date 2018/3/21 11:15
* @description
* @note
**/
public interface IWorker {
    /**
     * 在子线程中执行请求
     * @param request 当前需要执行的请求
     * @return 返回流
     * @throws Exception 请求失败
     */
    InputStream performRequest(Request request) throws Exception;

}
