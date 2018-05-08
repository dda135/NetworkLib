package indi.fanjh.networklib.urltransform;

/**
* @author fanjh
* @date 2018/3/21 10:53
* @description 进行Url转换
* @note 常用于结合不同的参数从而完成请求地址不同的场景
**/
public interface IUrlTransform {
    /**
     * 根据指定的参数或规则加工请求地址
     * @param originUrl 原始链接
     * @param param 需要处理的参数
     * @return 处理后的地址
     */
    String format(String originUrl, String... param);

}
