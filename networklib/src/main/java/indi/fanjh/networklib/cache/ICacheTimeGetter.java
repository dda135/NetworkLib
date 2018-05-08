package indi.fanjh.networklib.cache;

/**
* @author fanjh
* @date 2018/3/22 14:55
* @description 获取缓存时间，单位统一为ms
* @note 目前的处理是在最终缓存前再询问，这样可以方便请求完成后再确认缓存时长
**/
public interface ICacheTimeGetter<T> {
    /**
     * 获取缓存时长
     * @param object 解析后的结果，有的时候缓存时长会以这种模式返回
     * @return ms
     */
    int getTime(T object);

}
