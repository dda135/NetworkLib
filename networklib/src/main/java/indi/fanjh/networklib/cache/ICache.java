package indi.fanjh.networklib.cache;

/**
* @author fanjh
* @date 2018/3/22 11:30
* @description 缓存
* @note
**/
public interface ICache<T>{

    /**
     * 存储数据到缓存中
     * @param key 当前缓存key值
     * @param object 当前缓存数据
     * @param time 缓存时长ms
     */
    void put(String key, T object, int time);

    /**
     * 获取缓存数据
     * @param key 对应的缓存key值
     * @return 缓存的数据或者null
     */
    T get(String key);

}
