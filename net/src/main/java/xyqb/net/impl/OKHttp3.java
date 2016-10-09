package xyqb.net.impl;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import xyqb.net.HttpLog;
import xyqb.net.IRequest;
import xyqb.net.NetManager;
import xyqb.net.exception.HttpException;
import xyqb.net.model.HttpResponse;
import xyqb.net.model.RequestConfig;
import xyqb.net.model.RequestItem;
import xyqb.net.resultfilter.JsonParamsResultFilter;

/**
 * Created by cz on 8/23/16.
 */
public class OKHttp3 implements IRequest {
    private static final OkHttpClient httpClient;
    private static final RequestConfig requestConfig;
    private static final Interceptor DEFAULT_CACHE_CONTROL_INTERCEPTOR;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType STREAM=MediaType.parse("application/octet-stream");
    private static final HashMap<String,List<Call>> callItems;
    static {
        callItems=new HashMap<>();
        requestConfig = NetManager.getInstance().getRequestConfig();
        DEFAULT_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().removeHeader("Pragma")
                        .header("Cache-Control", String.format("max-age=%d", 10)).build();
            }
        };
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(requestConfig.connectTimeout, TimeUnit.SECONDS)
                .readTimeout(requestConfig.readTimeout, TimeUnit.SECONDS)
                .writeTimeout(requestConfig.writeTimeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(requestConfig.retryOnConnectionFailure)
                .addNetworkInterceptor(DEFAULT_CACHE_CONTROL_INTERCEPTOR);
        File cachedFile = requestConfig.cachedFile;
        if(null!=cachedFile&&cachedFile.exists()){
            long maxCacheSize = requestConfig.maxCacheSize;
            if(0==requestConfig.maxCacheSize){
                maxCacheSize=MAX_CACHE_SIZE;
            }
            clientBuilder.cache(new Cache(cachedFile, maxCacheSize));
        }
        setSslSocketFactory(clientBuilder);
        httpClient=clientBuilder.build();
    }
    public OKHttp3() {
    }

    /**
     * 设置HTTPS认证
     */
    private static void setSslSocketFactory(OkHttpClient.Builder clientBuilder){
        clientBuilder.hostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            sc.init(null,new TrustManager[]{trustManager}, new SecureRandom());
            clientBuilder.sslSocketFactory(sc.getSocketFactory(),trustManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public Observable<HttpResponse> call(String tag,final RequestItem item,final Object... values) {
        final HashMap<String,String> params=new HashMap<>();
        int length=Math.min(null==item.param?0:item.param.length, values.length);
        for(int i=0;i<length;i++){
            Object value = values[i];
            if(null!=value){
                params.put(item.param[i],value.toString());
            }
        }
        return call(tag, item, params);
    }

    @Override
    public void cancel(String tag) {
        List<Call> callsItem = callItems.get(tag);
        if(null!=callsItem){
            for(Iterator<Call> iterator=callsItem.iterator();iterator.hasNext();){
                Call call = iterator.next();
                iterator.remove();
                if(!call.isCanceled()){
                    call.cancel();
                }
            }
        }
    }


    public Observable<HttpResponse> call(final String tag,final RequestItem item, final HashMap<String,String> params){

        return Observable.create(new Observable.OnSubscribe<HttpResponse>() {
            @Override
            public void call(Subscriber<? super HttpResponse> subscriber) {
                Call call =null;
                try {
                    final Request request = getRequest(tag, item, params);
                    call = httpClient.newCall(request);
                    List<Call> calls = callItems.get(tag);
                    if(null==calls){
                        callItems.put(tag,calls=new ArrayList<>());
                    }
                    calls.add(call);
                    Response response = call.execute();
                    Headers headers = request.headers();
                    HttpResponse httpResponse = new HttpResponse();
                    if (null != headers&&0<headers.size()) {
                        Set<String> names = headers.names();
                        for (String item : names) {
                            httpResponse.headers.put(item, headers.get(item));
                        }
                    }
                    //url headers
                    String result = response.body().string();
                    if (response.isSuccessful()) {
                        removeCall(tag,call);
                        httpResponse.result = result;
                        subscriber.onNext(httpResponse);
                        if(null!=requestConfig.requestResultListener){
                            requestConfig.requestResultListener.onSuccess(httpResponse,item,request.url().toString());
                        }
                        HttpLog.d("Request success:"+item.info);
                    }else{
                        //request success but content is fail
                        removeCall(tag,call);
                        HashMap<String, String> params = new JsonParamsResultFilter().result(result);
                        HttpException exception = new HttpException();
                        String codeValue = params.get("code");
                        if(!TextUtils.isEmpty(codeValue)){
                            exception.code = Integer.valueOf(codeValue);
                        } else {
                            exception.code=IRequest.REQUEST_NO_CODE;
                        }
                        exception.message = params.get("message");
                        exception.headers = httpResponse.headers;
                        if(!params.isEmpty()){
                            exception.params.putAll(params);
                        }
                        exception.result = result;
                        if(null!=requestConfig.requestResultListener){
                            requestConfig.requestResultListener.onFailed(exception, item, request.url().toString());
                        }
                        subscriber.onError(exception);
                        HttpLog.d("Request failed:"+item.info+"\nMessage:"+exception.message+" code:"+exception.code);
                    }
                }
                catch(IOException e){
                    //request failed
                    removeCall(tag,call);
                    HttpException exception = new HttpException();
                    exception.code = IRequest.REQUEST_ERROR;
                    exception.message = e.getMessage();
                    if(null!=requestConfig.requestResultListener){
                        requestConfig.requestResultListener.onFailed(exception,item,item.url);
                    }
                    subscriber.onError(exception);
                    HttpLog.d("Request failed:"+item.info+"\nError:"+e.getMessage());
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private void removeCall(String tag,Call call) {
        if(null!=call){
            List<Call> callsItem = callItems.get(tag);
            if(null!=callsItem){
                callsItem.remove(call);
            }
        }
    }


    private Request getRequest(String tag,RequestItem item,Map<String, String> params){
        Request request;
        //add extras param
        if(null!=requestConfig&&null!=requestConfig.listener){
            HashMap<String, String> extraItems = requestConfig.listener.requestExtraItems();
            if(null!=extraItems&&!extraItems.isEmpty()){
                params.putAll(extraItems);
            }
        }
        String requestUrl = getRequestUrl(item);
        if(POST.equals(item.method)||PUT.equals(item.method)){
            RequestBody requestBody=null;
            if(null!=item.pathParams){
                requestUrl=String.format(requestUrl,item.pathParams);
            }
            if(!TextUtils.isEmpty(item.entity)){
                requestBody=RequestBody.create(JSON, item.entity);
                HttpLog.d("POST:"+requestUrl+" Json:\n"+item.entity);
            } else {
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                String formParams=new String();
                if(null!=params){
                    for (Map.Entry<String,String> entry:params.entrySet()) {
                        formParams+=(entry.getKey()+"="+entry.getValue()+"\n");
                        builder.addFormDataPart(entry.getKey(),entry.getValue());
                    }
                }
                if(!item.partBody.isEmpty()){
                    for (Map.Entry<String,File> entry:item.partBody.entrySet()) {
                        String name = entry.getKey();
                        File file = entry.getValue();
                        if(!TextUtils.isEmpty(name)&&null!=file&&file.exists()){
                            HttpLog.d("POST:"+requestUrl+" File:\n"+file.getAbsolutePath());
                            requestBody=RequestBody.create(STREAM, file);
                            builder.addFormDataPart(name,file.getName(),requestBody);
                        }
                    }
                }
                requestBody=builder.build();
                HttpLog.d(item.method+":"+requestUrl+" FROM:\n"+formParams);
            }

            Request.Builder requestBuilder = new Request.Builder().url(requestUrl);
            if(POST.equals(item.method)){
                requestBuilder.post(requestBody);
            } else if(PUT.equals(item.method)){
                requestBuilder.put(requestBody);
            }
            initRequestBuild(tag, item, requestBuilder);
            request=requestBuilder.build();
        } else {
            StringBuilder fullUrl = new StringBuilder(requestUrl);
            if(null!=item.pathParams){
                fullUrl.delete(0,fullUrl.length());
                fullUrl.append(String.format(requestUrl,item.pathParams));
            }
            if(null!=params){
                int index=0;
                int length=params.size();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    fullUrl.append(entry.getKey() + "=" + entry.getValue() + (index++ == length - 1 ? "" : "&"));
                }
            }

            HttpLog.d("Get:" + fullUrl.toString());
            Request.Builder requestBuilder = new Request.Builder().url(fullUrl.toString());
            initRequestBuild(tag, item, requestBuilder);
            request=requestBuilder.build();
        }
        //add cookie
        if (null!=item.cookies&&!item.cookies.isEmpty()) {
            Request.Builder newBuilder = request.newBuilder();
            String cookieValue = getCookieValue(item.cookies);
            newBuilder.header("Cookie", cookieValue);
            HttpLog.d("Add cookie:" + cookieValue);
            request=newBuilder.build();
        }
        return request;
    }

    private String getCookieValue(HashMap<String,String> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        for(Map.Entry<String,String> entry:cookies.entrySet()){
            cookieHeader.append(entry.getKey() + '=' + entry.getValue() + "; ");
        }
        return cookieHeader.toString();
    }


    private void initRequestBuild(String tag, RequestItem item, Request.Builder requestBuilder) {
        //add global header items
        if(null!=requestConfig&&null!=requestConfig.listener){
            HashMap<String, String> headerItems = requestConfig.listener.requestHeaderItems();
            if(null!=headerItems){
                for(Map.Entry<String,String> entry:headerItems.entrySet()){
                    requestBuilder.addHeader(entry.getKey(),entry.getValue());
                }
            }
        }
        //add custom header items
        if(null!=item.headers&&!item.headers.isEmpty()){
            for(Map.Entry<String,String> entry:item.headers.entrySet()){
                requestBuilder.addHeader(entry.getKey(),entry.getValue());
            }
        }
        if(null!=tag){
            requestBuilder.tag(tag);
        }
    }

    private String getRequestUrl(RequestItem item){
        String absoluteUrl;
        if(!item.url.startsWith("http")){
            absoluteUrl=requestConfig.url+item.url;
        } else if(!TextUtils.isEmpty(item.dynamicUrl)){
            absoluteUrl=item.dynamicUrl+item.url;
        } else {
            absoluteUrl=item.url;
        }
        return absoluteUrl;
    }
}
