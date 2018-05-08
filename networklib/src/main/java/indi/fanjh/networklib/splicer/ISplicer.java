package indi.fanjh.networklib.splicer;

import java.util.Map;

import indi.fanjh.networklib.Request;

/**
* @author fanjh
* @date 2018/3/21 10:42
* @description 参数拼接器
* @note 用于动态修改请求参数
**/
public interface ISplicer {
    /**
     * 处理原始参数
     * 可以替换、删除和添加等操作
     * @param request 当前请求
     * @param originValue 原始参数
     * @return 真实的请求参数
     */
    Map<String,String> split(Request request, Map<String, String> originValue);
}
