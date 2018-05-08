package indi.fanjh.networklib.exception;

/**
* @author fanjh
* @date 2018/3/22 14:27
* @description 当前只从缓存中获取，然而缓存中并没有想要的数据时候出现的异常
* @note
**/
public class CacheDataNotFoundException extends Exception{

    public CacheDataNotFoundException(String message) {
        super(message);
    }
}
