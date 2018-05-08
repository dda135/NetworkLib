package indi.fanjh.networklib;

import indi.fanjh.networklib.cache.ICache;
import indi.fanjh.networklib.exception.CacheDataNotFoundException;

/**
 * @author fanjh
 * @date 2018/3/22 14:18
 * @description 请求分发者工厂
 * @note
 **/
class RequestDispatcherFactory {

    static <T> RequestDispatcher<T> getDispatcher(@RequestStrategy int strategy) {
        switch (strategy) {
            case RequestStrategy.COMMON:
                return new RequestDispatcher<T>() {
                    @Override
                    void start(Request<T> request, ICache<T> cache) {
                        request.requestNetwork();
                    }

                    @Override
                    void successEnd(Request<T> request, ICache<T> cache, T object, int cacheTime) {
                        request.deliverySuccess(object, ResultFrom.FROM_NETWORK);
                    }

                    @Override
                    void errorEnd(Request<T> request, Throwable ex) {
                        request.deliveryError(ex, ResultFrom.FROM_NETWORK);
                    }

                };
            case RequestStrategy.ONLY_GET_CACHE:
                return new RequestDispatcher<T>() {
                    @Override
                    void start(final Request<T> request, final ICache<T> cache) {
                        if(null == cache){
                            throw new IllegalArgumentException("请先设置缓存执行类！");
                        }
                        if(null == request.getCacheKey()){
                            throw new IllegalArgumentException("缓存必须设置key值！");
                        }
                        CACHE_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                T object = null;
                                try {
                                    object = cache.get(request.getCacheKey());
                                }catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                if (null != object) {
                                    request.deliverySuccess(object, ResultFrom.FROM_DISK_CACHE);
                                } else {
                                    request.deliveryError(new CacheDataNotFoundException("缓存中并没有数据！"), ResultFrom.FROM_DISK_CACHE);
                                }
                            }
                        });
                    }

                    @Override
                    void successEnd(Request<T> request, ICache<T> cache, T object, int cacheTime) {

                    }

                    @Override
                    void errorEnd(Request<T> request, Throwable ex) {

                    }

                };
            case RequestStrategy.ONLY_PUT_CACHE:
                return new RequestDispatcher<T>() {
                    @Override
                    void start(Request<T> request, ICache<T> cache) {
                        request.requestNetwork();
                    }

                    @Override
                    void successEnd(final Request<T> request, final ICache<T> cache, final T object, final int cacheTime) {
                        if(null == cache){
                            throw new IllegalArgumentException("请先设置缓存执行类！");
                        }
                        if(null == request.getCacheKey()){
                            throw new IllegalArgumentException("缓存必须设置key值！");
                        }
                        CACHE_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                cache.put(request.getCacheKey(), object, cacheTime);
                            }
                        });
                    }

                    @Override
                    void errorEnd(Request<T> request, Throwable ex) {

                    }
                };
            case RequestStrategy.FIRST_CACHE_NEXT_NETWORK:
                return new RequestDispatcher<T>() {
                    @Override
                    void start(final Request<T> request, final ICache<T> cache) {
                        if(null == cache){
                            throw new IllegalArgumentException("请先设置缓存执行类！");
                        }
                        if(null == request.getCacheKey()){
                            throw new IllegalArgumentException("缓存必须设置key值！");
                        }
                        CACHE_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                T object = null;
                                try {
                                    object = cache.get(request.getCacheKey());
                                }catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                if (null != object) {
                                    request.deliverySuccess(object, ResultFrom.FROM_DISK_CACHE);
                                }
                                request.requestNetwork();
                            }
                        });
                    }

                    @Override
                    void successEnd(final Request<T> request, final ICache<T> cache, final T object, final int cacheTime) {
                        if(null == cache){
                            throw new IllegalArgumentException("请先设置缓存执行类！");
                        }
                        if(null == request.getCacheKey()){
                            throw new IllegalArgumentException("缓存必须设置key值！");
                        }
                        CACHE_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                cache.put(request.getCacheKey(), object, cacheTime);
                            }
                        });
                        request.deliverySuccess(object, ResultFrom.FROM_NETWORK);
                    }

                    @Override
                    void errorEnd(Request<T> request, Throwable ex) {
                        request.deliveryError(ex, ResultFrom.FROM_NETWORK);
                    }

                };
            case RequestStrategy.COMMON_CACHE:
                return new RequestDispatcher<T>() {
                    @Override
                    void start(final Request<T> request, final ICache<T> cache) {
                        if(null == cache){
                            throw new IllegalArgumentException("请先设置缓存执行类！");
                        }
                        if(null == request.getCacheKey()){
                            throw new IllegalArgumentException("缓存必须设置key值！");
                        }
                        CACHE_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                T object = null;
                                try {
                                    object = cache.get(request.getCacheKey());
                                }catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                if (null != object) {
                                    request.deliverySuccess(object, ResultFrom.FROM_DISK_CACHE);
                                } else {
                                    request.requestNetwork();
                                }
                            }
                        });
                    }

                    @Override
                    void successEnd(final Request<T> request, final ICache<T> cache, final T object, final int cacheTime) {
                        if(null == cache){
                            throw new IllegalArgumentException("请先设置缓存执行类！");
                        }
                        if(null == request.getCacheKey()){
                            throw new IllegalArgumentException("缓存必须设置key值！");
                        }
                        CACHE_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                cache.put(request.getCacheKey(), object, cacheTime);
                            }
                        });
                        request.deliverySuccess(object, ResultFrom.FROM_NETWORK);
                    }

                    @Override
                    void errorEnd(Request<T> request, Throwable ex) {
                        request.deliveryError(ex, ResultFrom.FROM_NETWORK);
                    }

                };
            default:
                throw new IllegalArgumentException("当前缓存策略无法识别！");
        }
    }

}
