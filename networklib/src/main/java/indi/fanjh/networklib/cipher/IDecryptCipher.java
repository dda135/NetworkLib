package indi.fanjh.networklib.cipher;

/**
* @author fanjh
* @date 2018/3/21 14:18
* @description 解密套件
* @note 用于将返回的数据解密
**/
public interface IDecryptCipher {
    /**
     * 对返回数据进行解密操作
     * @param cipherText 密文
     * @return 明文
     */
    String decrypt(String cipherText);

}
