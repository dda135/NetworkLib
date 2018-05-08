package indi.fanjh.networklib.cipher;

/**
* @author fanjh
* @date 2018/3/21 10:32
* @description 加密套件
* @note 用于处理对文本数据的操作（不处理文件的二进制数据）
**/
public interface IEncryptCipher {
    /**
     * 数据传递模式为key-value，这里是对value进行加密
     * @param key 当前参数的key值
     * @param originValue 明文，原始字符串
     * @return 加密后的value字符串
     */
    String encrypt(String key, String originValue);
}
