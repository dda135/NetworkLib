package indi.fanjh.networklib.converter;


import java.io.InputStream;
import java.lang.reflect.Type;

import indi.fanjh.networklib.Request;
import indi.fanjh.networklib.cipher.IDecryptCipher;


/**
* @author fanjh
* @date 2018/3/23 10:11
* @description 数据转换器
* @note 用于将网络请求返回的流数据转换为想要的数据
 * 比方说转换为string、bitmap等等
**/
public interface IConverter<T> {

    /**
     * 将网络请求返回的字符串转换为指定对象
     * @param request 当前请求
     * @param inputStream 网络请求返回的流
     * @param decryptCipher 可能用到的解密套件
     * @param type 最终的解析类型
     * @return 指定对象
     * @throws  Exception 解析中出现异常
     */
    T convert(Request request, InputStream inputStream, IDecryptCipher decryptCipher, Type type) throws Exception;

}
