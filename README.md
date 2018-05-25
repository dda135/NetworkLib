# NetworkLib
一个网络请求组件库，通过拆分功能接口和Builder模式，希望能够在使用简单的基础上，具有更好的灵活性

## 概述
一个项目级的框架，至少要有几个考量：
1.封装性：对于外部的使用者来说，大部分的功能应该是不透明，只提供简易的API来使用<br>
内部封装不少核心功能，比方说自定义缓存这块对于外部来说完全封闭，只是提供了方式给予使用，从而内部的修改等对于外部来说完全不可知<br>
API接口采用Builder的方式，阅读起来相对清晰并且添加和删除比较简单<br>
2.扩展性：基于已有的功能，能否在继续扩展功能使用，并且能够不破坏其他使用地方<br>
由于封装性的存在，所以内部进行修改对于外部来说不可知，这也能够方便进行扩展<br>
内部采用组件和接口的方式，从而能够使得组件更加容易的进行扩展和替换<br>
3.灵活性：同样的功能能够很快的替换为不同的组件使用，并且使用中可以很容易的进行设置和修改<br>
目前考量是通过注册的方式来实现大量通用组件的实现，然后通过常用的链式调用来使用，至少能够使得通用<br>
并且可以通过单次使用注入的方式，允许自定义组件来实现单次的特殊操作，否则默认采用预注册组件的方式来实现<br>
大量接口都采用组合的模式，从而能够更加灵活的添加、删除和修改<br>

## 已有功能说明
1.支持自定义请求地址，并且可以对地址做自定义操作<br>
2.支持对输入参数进行通用转换和单个字段加密操作<br>
3.支持对返回结果的统一解密<br>
4.支持自定义实际的网络请求实现类，比方说使用Okhttp或者HttpsUrlConnection等来实现<br>
5.支持自定义缓存实现类，并且缓存时间可以自定义或者从请求结果中获取，因为部分请求并没有按照cache-control来做，那么大部分会使用在返回结果中添加缓存时间字段的方式来实现<br>
6.支持自定义对返回结果的操作，比方说通过gson解析直接获得实体类结果或者说下载图片转换为bitmap等等<br>
7.支持设置通用组件和自定义单次使用组件<br>

## 简单的例子
1.使用前最好进行通用配置，因为大部分项目请求在很多地方都是一套，个别特殊的可以自定义组件来处理，这个后面再说
```
/**
* 应用Application入口
*/
public class MainApplication extends BaseApplication {
    @Override
    public void onCreate() {
        Request.setCommonUrl(NetworkConfig.COMMON_URL);
        Request.setDefaultTransform(new CustomUrlTransform());
        Request.setDefaultSplicer(new CustomSplicer());
        CustomCipher customCipher = new CustomCipher();
        Request.setDefaultEncryptCipher(customCipher);
        Request.setDefaultDecryptCipher(customCipher);
        Request.setDefaultCache(ACache.get(APPLICATION_CONTEXT, NetworkConfig.CACHE_DIR));
        Request.setDefaultConverter(new CustomConverter());
    }
}

```
比方说这里就定义了请求的通用链接以及转换器，举个例子说明
```
public static String COMMON_URL = BuildConfig.COMMON_HOST + "/%1$s/%2$s";

public class CustomUrlTransform implements IUrlTransform{
    @Override
    public String format(String originUrl, String... param) {
        if (param.length != 2) {
            throw new IllegalArgumentException("必须两个控制参数！");
        }
        //做url的format操作
        return String.format(originUrl, param[0], param[1]);
    }
}
```
这样相当于就定义了每次请求的基础链接和转换器，相当于是一个链接加控制器和Action的组合<br>
看一个简单的使用例子
```
    //文本参数
    Map<String, String> map = new HashMap<>();
    map.put("xxx", ...);
    ...添加参数
    //文件参数
    Map<String, String> fileMap = new HashMap<>();
    map.put("xxx", ...);
    ...添加参数
    new Request.Builder<xxx>()//通过泛型指定想要的结果对象
        .urlTransform("xxx", "xxx")//结合BaseUrl及通用的IUrlTransform指定不用的Controller和Action
        .processListener(new CustomLoadingListener(mContext))//监听请求开始和结束，一般用于对话框展示和隐藏
        .errorListener(new CustomStatusErrorListener())//请求失败，比方说网络异常或者解析数据失败等等
        .fileParams(fileMap)//上传指定的文件，key是文件上传时候的key，value是文件在本地的路径
        .stringParams(map)//上传指定的参数
        .build()
        .execute(new ISuccessListener<BaseEntity>() {
            @Override
            public void onCall(BaseEntity object, int from) {
                ...//最终结果处理           
            }});  
```

