package indi.fanjh.networklib.worker;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import indi.fanjh.networklib.Request;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
* @author fanjh
* @date 2018/3/21 11:45
* @description 基于OKHttp3实现的请求
* @note
**/
public class OkHttpWorker implements IWorker{
    public static final MediaType FILE_TYPE = MediaType.parse("application/octet-stream");
    private static OkHttpClient OK_HTTP_CLIENT;

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().
                readTimeout(Request.READ_TIMEOUT, TimeUnit.SECONDS).
                connectTimeout(Request.CONNECT_TIMEOUT, TimeUnit.SECONDS).
                writeTimeout(Request.WRITE_TIMEOUT, TimeUnit.SECONDS);
        initClient(builder);
        OK_HTTP_CLIENT = builder.build();
    }

    @Override
    public InputStream performRequest(Request request) throws Exception{
        URL url = new URL(request.getTrueUrl());

        okhttp3.Request.Builder okRequestBuilder = null;

        if(request.isGet()){
            okRequestBuilder = new okhttp3.Request.Builder().
                    get().
                    url(url);
        }else {
            MultipartBody.Builder requestBuilder = new MultipartBody.Builder();

            requestBuilder.setType(MultipartBody.FORM);

            Map<String, File> fileParams = request.getFileParams();
            if (null != fileParams) {
                for (Map.Entry<String, File> entry : fileParams.entrySet()) {
                    File file = entry.getValue();
                    requestBuilder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(FILE_TYPE, file));
                }
            }

            Map<String, String> params = request.getEncryptParam();
            if (null != params) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    requestBuilder.addFormDataPart(entry.getKey(), entry.getValue());
                }
            }
            //为了避免SNI问题，必须指定HOST从而帮助服务端选定证书
            okRequestBuilder = new okhttp3.Request.Builder().
                    url(url).
                    addHeader("HOST", url.getHost()).
                    post(requestBuilder.build());

        }

        if(null != request.getTag()){
            okRequestBuilder.tag(request.getTag());
        }

        Map<String,String> headers = request.getHeaders();
        if(null != headers){
            for(Map.Entry<String,String> entry:headers.entrySet()){
                okRequestBuilder.addHeader(entry.getKey(),entry.getValue());
            }
        }

        OkHttpClient client = OK_HTTP_CLIENT;

        //目前版本的OkHttp要求超时时间不同必须新建Client
        if(request.getConnectTimeout() != Request.CONNECT_TIMEOUT ||
                request.getWriteTimeout() != Request.WRITE_TIMEOUT ||
                request.getReadTimeout() != Request.READ_TIMEOUT){
            OkHttpClient.Builder builder = new OkHttpClient.Builder().
                    connectTimeout(request.getConnectTimeout(),TimeUnit.SECONDS).
                    readTimeout(request.getReadTimeout(),TimeUnit.SECONDS).
                    writeTimeout(request.getWriteTimeout(),TimeUnit.SECONDS);
            initClient(builder);
            client = builder.build();
        }

        Response response = client.newCall(okRequestBuilder.build()).execute();
        if(response.isSuccessful()){
            ResponseBody responseBody = response.body();
            if(null != responseBody){
                return responseBody.byteStream();
            }
        }

        throw new Exception("failed");
    }

    /**
     * 用于配置Client的通用配置
     * @param builder
     */
    private static void initClient(OkHttpClient.Builder builder){
        //预留
    }

}
