package indi.fanjh.networklib;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import indi.fanjh.networklib.cache.ICache;
import indi.fanjh.networklib.cache.ICacheTimeGetter;
import indi.fanjh.networklib.cipher.IDecryptCipher;
import indi.fanjh.networklib.cipher.IEncryptCipher;
import indi.fanjh.networklib.converter.IConverter;
import indi.fanjh.networklib.delivery.DefaultDelivery;
import indi.fanjh.networklib.delivery.IDelivery;
import indi.fanjh.networklib.exception.ConvertException;
import indi.fanjh.networklib.exception.FileUnableUploadException;
import indi.fanjh.networklib.listener.IErrorListener;
import indi.fanjh.networklib.listener.IProcessListener;
import indi.fanjh.networklib.listener.ISuccessListener;
import indi.fanjh.networklib.splicer.ISplicer;
import indi.fanjh.networklib.urltransform.IUrlTransform;
import indi.fanjh.networklib.worker.IWorker;
import indi.fanjh.networklib.worker.OkHttpWorker;


/**
 * @author fanjh
 * @date 2018/3/21 11:15
 * @description 请求者
 * @note
 **/
public class Request<T> implements Runnable,Comparable<Request>{
    /**
     * 默认从服务端读取返回值超时15s
     */
    public static final int READ_TIMEOUT = 15;
    /**
     * 默认连接超时15s
     */
    public static final int CONNECT_TIMEOUT = 15;
    /**
     * 默认向服务端写入参数的超时为15s
     */
    public static final int WRITE_TIMEOUT = 15;
    /**
     * 主线程Handler
     */
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    /**
     * 用于存储当前进行中的任务（请求）
     * 存在多线程情况
     */
    private static final ConcurrentLinkedQueue<Request> RUNNING_TASK = new ConcurrentLinkedQueue<>();
    /**
     * 当前默认核心线程数为N+1
     */
    private static final int CORE_SIZE = Runtime.getRuntime().availableProcessors() + 1;
    private static final ThreadPoolExecutor NETWORK_EXECUTOR = new ThreadPoolExecutor(CORE_SIZE, CORE_SIZE, 5, TimeUnit.MINUTES,
            new PriorityBlockingQueue<Runnable>(), new ThreadFactory() {
        private AtomicInteger atomicInteger = new AtomicInteger(0);
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("network_executor_" + atomicInteger.decrementAndGet());
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    });
    private static ICache DEFAULT_CACHE;
    private static IDelivery DEFAULT_DELIVERY = new DefaultDelivery<>();
    private static IWorker DEFAULT_WORKER = new OkHttpWorker();
    private static String COMMON_URL;
    private static IUrlTransform DEFAULT_TRANSFORM = new IUrlTransform() {
        @Override
        public String format(String originUrl, String... param) {
            return originUrl;
        }
    };
    private static IEncryptCipher DEFAULT_ENCRYPT_CIPHER = new IEncryptCipher() {
        @Override
        public String encrypt(String key, String originValue) {
            return originValue;
        }
    };
    private static IDecryptCipher DEFAULT_DECRYPT_CIPHER = new IDecryptCipher() {
        @Override
        public String decrypt(String cipherText) {
            return cipherText;
        }
    };
    private static IConverter DEFAULT_CONVERTER = new IConverter() {
        @Override
        public Object convert(Request request,InputStream inputStream, IDecryptCipher decryptCipher, Type type) throws ConvertException {
            throw new ConvertException(new IllegalArgumentException("默认不知道如何解析你的数据！"));
        }
    };
    private static ISplicer DEFAULT_SPLICER = new ISplicer() {
        @Override
        public Map<String, String> split(Request request,Map<String, String> originValue) {
            return originValue;
        }
    };
    private static ICacheTimeGetter DEFAULT_CACHE_TIME_GETTER = new ICacheTimeGetter() {
        @Override
        public int getTime(Object o) {
            //默认一分钟
            return 1000 * 60;
        }
    };
    private static IProcessListener DEFAULT_PROCESS_LISTENER;
    /**
     * 文件参数
     * key：文件上传的key值
     * value：文件在当前机器上面的路径
     */
    private Map<String, String> fileParams;
    /**
     * 文本参数
     * key：当前参数的key值
     * value：当前参数的实际值
     */
    private Map<String, String> stringParams;
    /**
     * 当次请求需要添加的头部报文
     */
    private Map<String, String> headers;
    /**
     * 当前请求的基本链接
     */
    private String baseUrl;
    /**
     * 作为参数结合{@link IUrlTransform}进行baseUrl的转换
     */
    private String[] urlTransformParams;
    /**
     * 结合urlTransformParams进行baseUrl的转换
     */
    private IUrlTransform urlTransform;
    /**
     * 当次请求所使用的缓存策略
     */
    @RequestStrategy
    private int requestStrategy = RequestStrategy.COMMON;
    /**
     * 当次请求参数的转换器
     */
    private ISplicer splicer;
    /**
     * 当次请求结果的转换器
     */
    private IConverter<T> converter;
    /**
     * 当次请求中对参数的加解密处理
     */
    private IEncryptCipher encryptCipher;
    /**
     * 当次请求中对返回数据的解密处理
     */
    private IDecryptCipher decryptCipher;
    /**
     * 当次请求中使用的缓存执行者
     */
    private ICache<T> cache;
    /**
     * 连接到服务端的超时s
     */
    private int connectTimeout;
    /**
     * 将数据写到服务端的超时s
     */
    private int writeTimeout;
    /**
     * 从服务端读取数据的超时s
     */
    private int readTimeout;
    /**
     * 当次请求的标记，用于标示一个请求，可以进行暂停、取消等操作
     */
    private String tag;
    /**
     * 当次请求的实际执行者
     */
    private IWorker worker;
    /**
     * 当次请求的投递者
     */
    private IDelivery<T> delivery;
    /**
     * 请求失败进行结果回调
     */
    private ISuccessListener<T> successListener;
    /**
     * 请求失败进行结果回调
     */
    private IErrorListener errorListener;
    /**
     * 请求开始和结束回调
     */
    private IProcessListener processListener;
    /**
     * 是否回调结果在子线程
     * 只有在使用默认Delivery的时候有效
     */
    private boolean callResultAtBackground;
    /**
     * 缓存的key值
     */
    private String cacheKey;
    /**
     * 当前请求是否被取消
     */
    private boolean isCancelled;
    /**
     * 当前请求优先级
     */
    private @RequestPriority int priority;
    /**
     * 当前请求的分发者
     */
    private RequestDispatcher<T> requestDispatcher;
    /**
     * 缓存时间获取者
     */
    private ICacheTimeGetter<T> cacheTimeGetter;
    /**
     * 生命周期，其实意思就是当关联对象没有被回收的时候请求有效
     * 否则无效（可能不进行回调，从而避免异步的空指针问题）
     */
    private WeakReference<Object> lifeCycle;
    /**
     * 是否get请求
     */
    private boolean isGet;

    Request() {
    }

    public static void setDefaultDelivery(IDelivery defaultDelivery) {
        DEFAULT_DELIVERY = defaultDelivery;
    }

    public static void setDefaultWorker(IWorker defaultWorker) {
        DEFAULT_WORKER = defaultWorker;
    }

    public static void setDefaultProcessListener(IProcessListener defaultProcessListener) {
        DEFAULT_PROCESS_LISTENER = defaultProcessListener;
    }

    public static void setCommonUrl(String commonUrl) {
        COMMON_URL = commonUrl;
    }

    public static void setDefaultTransform(IUrlTransform defaultTransform) {
        DEFAULT_TRANSFORM = defaultTransform;
    }

    public static void setDefaultEncryptCipher(IEncryptCipher defaultEncryptCipher) {
        DEFAULT_ENCRYPT_CIPHER = defaultEncryptCipher;
    }

    public static void setDefaultDecryptCipher(IDecryptCipher defaultDecryptCipher) {
        DEFAULT_DECRYPT_CIPHER = defaultDecryptCipher;
    }

    public static void setDefaultConverter(IConverter defaultConverter) {
        DEFAULT_CONVERTER = defaultConverter;
    }

    public static void setDefaultSplicer(ISplicer defaultSplicer) {
        DEFAULT_SPLICER = defaultSplicer;
    }

    public static void setDefaultCache(ICache defaultCache) {
        DEFAULT_CACHE = defaultCache;
    }

    public static void setDefaultCacheTimeGetter(ICacheTimeGetter defaultCacheTimeGetter) {
        DEFAULT_CACHE_TIME_GETTER = defaultCacheTimeGetter;
    }

    public static void cancelAllRequest(){
        Iterator<Request> iterator = RUNNING_TASK.iterator();
        while(iterator.hasNext()){
            Request request = iterator.next();
            request.isCancelled = true;
        }
    }

    public static void cancelRequestByTag(String tag){
        if(null == tag){
            return;
        }
        //这里实际上也没有强行终止，而是通过cancelled标记进行自然终止
        Iterator<Request> iterator = RUNNING_TASK.iterator();
        while(iterator.hasNext()){
            Request request = iterator.next();
            if(tag.equals(request.tag)){
                request.isCancelled = true;
            }
        }
    }

    @Override
    public int compareTo(@NonNull Request o) {
        int minePriority = priority;
        int otherPriority = o.priority;
        if(minePriority > otherPriority){
            return 1;
        }else if(minePriority == otherPriority){
            return 0;
        }
        return -1;
    }

    /**
     * 获取当次请求的真实链接地址
     *
     * @return 链接地址
     */
    public String getTrueUrl() {
        return urlTransform.format(baseUrl, urlTransformParams);
    }

    /**
     * 获取当次请求所需要上传的文件数据
     *
     * @return Map key：上传的key value：文件的路径
     */
    public Map<String, File> getFileParams() throws Exception {
        if (null != fileParams) {
            Map<String, File> fileMap = new HashMap<>();
            for (Map.Entry<String, String> entry : fileParams.entrySet()) {
                File file = new File(entry.getValue());
                if (!file.exists() || !file.isFile()) {
                    throw new FileUnableUploadException("要上传的文件不存在！");
                }
                fileMap.put(entry.getKey(), file);
            }
            return fileMap;
        }
        return null;
    }

    /**
     * 获取加密和处理后的参数
     *
     * @return Map key：上传的key value：加密后的值
     */
    public Map<String, String> getEncryptParam() {
        stringParams = splicer.split(this,stringParams);
        for (Map.Entry<String, String> entry : stringParams.entrySet()) {
            entry.setValue(encryptCipher.encrypt(entry.getKey(), entry.getValue()));
        }
        return stringParams;
    }

    /**
     * 获得当次请求所需的头部报文
     *
     * @return Map key：头部报文的key value：头部报文的值
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getTag() {
        return tag;
    }

    public ISuccessListener<T> getSuccessListener() {
        return successListener;
    }

    public IErrorListener getErrorListener() {
        return errorListener;
    }

    public int getConnectTimeout() {
        return connectTimeout <= 0?CONNECT_TIMEOUT:connectTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout <= 0?WRITE_TIMEOUT:writeTimeout;
    }

    public int getReadTimeout() {
        return readTimeout <= 0?READ_TIMEOUT:readTimeout;
    }

    String getCacheKey() {
        return cacheKey;
    }

    public void execute(ISuccessListener<T> successListener) {
        if(null == successListener){
            throw new NullPointerException("结果监听不能为空！可以为空操作");
        }
        if(null != processListener) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                MAIN_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        processListener.onStart();
                    }
                });
            }else{
                processListener.onStart();
            }
        }
        RUNNING_TASK.add(this);
        this.successListener = successListener;
        requestDispatcher = RequestDispatcherFactory.getDispatcher(requestStrategy);
        requestDispatcher.start(this,cache);
    }

    void requestNetwork(){
        if(isCancelled()){
            endTask();
            return;
        }
        NETWORK_EXECUTOR.execute(this);
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        try {
            if(isCancelled()){
                endTask();
                return;
            }
            inputStream = worker.performRequest(this);
            if(isCancelled()){
                endTask();
                return;
            }
            Type type = ((ParameterizedType)successListener.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
            T object = converter.convert(this,inputStream,decryptCipher, type);
            if(isCancelled()){
                endTask();
                return;
            }
            requestDispatcher.successEnd(this,cache,object,cacheTimeGetter.getTime(object));
            endTask();
        } catch (Throwable e) {
            e.printStackTrace();
            if(isCancelled()){
                endTask();
                return;
            }
            requestDispatcher.errorEnd(this,e);
            endTask();
        } finally {
            if(null != inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 当前请求是否取消
     * @return true表示已经取消
     */
    private boolean isCancelled(){
        if(isCancelled){
            return true;
        }
        if(null != lifeCycle){
            return null == lifeCycle.get();
        }
        return false;
    }

    private void endTask(){
        RUNNING_TASK.remove(this);
        if(null != processListener){
            if(Looper.myLooper() != Looper.getMainLooper()){
                MAIN_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        processListener.onEnd();
                        processListener = null;
                    }
                });
            }else{
                processListener.onEnd();
                processListener = null;
            }
        }
    }

    void deliverySuccess(T object,@ResultFrom int form){
        if(null != successListener){
            delivery.deliverySuccessObject(this,object,form);
        }
    }

    void deliveryError(Throwable ex,@ResultFrom int form){
        if(null != errorListener){
            delivery.deliveryError(this,ex,form);
        }
    }

    public String getUrlTransform(){
        if(null == urlTransformParams){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(String temp:urlTransformParams){
            stringBuilder.append(temp).append("_");
        }
        return stringBuilder.toString();
    }

    public boolean isGet() {
        return isGet;
    }

    /**
     * 通过Builder来简化使用和方便扩展
     * @param <T> 当前请求所想要的结果
     */
    public static final class Builder<T> {
        private Map<String, String> fileParams;
        private Map<String, String> stringParams;
        private Map<String, String> headers;
        private String baseUrl;
        private String[] urlTransformParams;
        private IUrlTransform urlTransform;
        private int requestStrategy;
        private ISplicer splicer;
        private IConverter<T> converter;
        private IEncryptCipher encryptCipher;
        private IDecryptCipher decryptCipher;
        private ICache<T> cache;
        private int connectTimeout;
        private int writeTimeout;
        private int readTimeout;
        private String tag;
        private IDelivery<T> delivery;
        private IErrorListener errorListener;
        private IProcessListener processListener;
        private boolean callResultAtBackground;
        private String cacheKey;
        private int priority;
        private ICacheTimeGetter<T> cacheTimeGetter;
        private IWorker worker;
        private Object lifeCycleItem;
        private boolean isGet;

        public Builder<T> get() {
            isGet = true;
            return this;
        }

        public Builder<T> with(Object val) {
            lifeCycleItem = val;
            return this;
        }

        public Builder<T> fileParams(Map<String, String> val) {
            fileParams = val;
            return this;
        }

        public Builder<T> stringParams(Map<String, String> val) {
            stringParams = val;
            return this;
        }

        public Builder<T> headers(Map<String, String> val) {
            headers = val;
            return this;
        }

        public Builder<T> baseUrl(String val) {
            baseUrl = val;
            return this;
        }

        public Builder<T> urlTransform(String... val) {
            urlTransformParams = val;
            return this;
        }

        public Builder<T> urlTransform(IUrlTransform val,String... val1) {
            urlTransform = val;
            urlTransformParams = val1;
            return this;
        }

        public Builder<T> requestStrategy(@RequestStrategy int val) {
            requestStrategy = val;
            return this;
        }

        public Builder<T> splicer(ISplicer val) {
            splicer = val;
            return this;
        }

        public Builder<T> converter(IConverter<T> val) {
            converter = val;
            return this;
        }

        public Builder<T> worker(IWorker val) {
            worker = val;
            return this;
        }

        public Builder<T> encryptCipher(IEncryptCipher val) {
            encryptCipher = val;
            return this;
        }

        public Builder<T> decryptCipher(IDecryptCipher val) {
            decryptCipher = val;
            return this;
        }

        public Builder<T> cache(ICache<T> val) {
            cache = val;
            return this;
        }

        public Builder<T> connectTimeout(int val) {
            connectTimeout = val;
            return this;
        }

        public Builder<T> writeTimeout(int val) {
            writeTimeout = val;
            return this;
        }

        public Builder<T> readTimeout(int val) {
            readTimeout = val;
            return this;
        }

        public Builder<T> tag(String val) {
            tag = val;
            return this;
        }

        public Builder<T> delivery(IDelivery<T> val) {
            delivery = val;
            return this;
        }

        public Builder<T> errorListener(IErrorListener val) {
            errorListener = val;
            return this;
        }

        public Builder<T> processListener(IProcessListener val) {
            processListener = val;
            return this;
        }

        public Builder<T> callResultAtBackground(boolean val) {
            callResultAtBackground = val;
            return this;
        }

        public Builder<T> cacheKey(String val) {
            cacheKey = val;
            return this;
        }

        public Builder<T> priority(int val) {
            priority = val;
            return this;
        }

        public Builder<T> cacheTimeGetter(ICacheTimeGetter<T> val) {
            cacheTimeGetter = val;
            return this;
        }

        public Request<T> build() {
            Request<T> request = new Request<>();
            request.fileParams = fileParams;
            request.stringParams = stringParams;
            request.headers = headers;
            request.baseUrl = null == baseUrl?COMMON_URL:baseUrl;
            request.urlTransformParams = urlTransformParams;
            request.urlTransform = null == urlTransform?DEFAULT_TRANSFORM:urlTransform;
            request.requestStrategy = requestStrategy;
            request.splicer = null == splicer?DEFAULT_SPLICER:splicer;
            request.converter = null == converter?DEFAULT_CONVERTER:converter;
            request.encryptCipher = null == encryptCipher?DEFAULT_ENCRYPT_CIPHER:encryptCipher;
            request.decryptCipher = null == decryptCipher?DEFAULT_DECRYPT_CIPHER:decryptCipher;
            request.cache = null == cache?DEFAULT_CACHE:cache;
            request.connectTimeout = connectTimeout;
            request.writeTimeout = writeTimeout;
            request.readTimeout = readTimeout;
            request.tag = tag;
            if(null == delivery){
                request.delivery = DEFAULT_DELIVERY;
                ((DefaultDelivery)request.delivery).setCallAtBackground(callResultAtBackground);
            }else{
                request.delivery = delivery;
            }
            request.callResultAtBackground = callResultAtBackground;
            request.errorListener = errorListener;
            request.cacheKey = cacheKey;
            request.priority = priority;
            request.cacheTimeGetter = null == cacheTimeGetter?DEFAULT_CACHE_TIME_GETTER:cacheTimeGetter;
            request.processListener = null == processListener?DEFAULT_PROCESS_LISTENER:processListener;
            request.worker = null == worker?DEFAULT_WORKER:worker;
            if(null != lifeCycleItem){
                request.lifeCycle = new WeakReference<>(lifeCycleItem);
            }
            request.isGet = isGet;
            return request;
        }
    }
}
