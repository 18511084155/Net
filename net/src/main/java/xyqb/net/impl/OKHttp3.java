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
import xyqb.library.XmlElement;
import xyqb.library.config.XmlReaderBase;
import xyqb.net.IRequest;
import xyqb.net.NetManager;
import xyqb.net.callback.OnClientCreateCallback;
import xyqb.net.exception.HttpException;
import xyqb.net.log.HttpLog;
import xyqb.net.model.HttpResponse;
import xyqb.net.model.RequestConfig;
import xyqb.net.model.RequestItem;
import xyqb.net.resultfilter.JsonParamsResultFilter;
import xyqb.net.util.NetUtils;
import xyqb.net.xml.RequestConfigReader;



/**
 * Created by cz on 8/23/16.
 */
public class OKHttp3 implements IRequest {
    private static final OkHttpClient httpClient;
    private static final RequestConfig requestConfig;
    private static final Interceptor DEFAULT_CACHE_CONTROL_INTERCEPTOR;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType TEXT = MediaType.parse("Content-Type application/x-www-form-");
    private static final MediaType STREAM=MediaType.parse("application/octet-stream");
    private static final HashMap<String,List<Call>> callItems;
    private static final HashMap<String,RequestItem> cacheItems;
    private static final RequestConfigReader configReader;

    static {
        callItems=new HashMap<>();
        cacheItems=new HashMap<>();
        configReader=new RequestConfigReader();
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
        OnClientCreateCallback createCallback = requestConfig.clientCreateCallback;
        if(null!=createCallback){
            createCallback.onCallback(clientBuilder);
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
        ensureRequestItems(item);
        final HashMap<String,String> params=new HashMap<>();
        int length=Math.min(null==item.param?0:item.param.length, values.length);
        for(int i=0;i<length;i++){
            Object value = values[i];
            if(null!=value){
                params.put(item.param[i],value.toString());
            }
        }
        if(null!=item.params&&!item.params.isEmpty()){
            params.putAll(item.params);
        }
        return call(tag,item, params);
    }

    @Override
    public void cancel(String tag) {
        List<Call> callsItem = callItems.get(tag);
        HttpLog.d("Request cancel:"+tag+" count:"+(null==callsItem?0:callsItem.size()));
        if(null!=callsItem){
            StringBuilder builder=new StringBuilder();
            for(Iterator<Call> iterator=callsItem.iterator();iterator.hasNext();){
                Call call = iterator.next();
                iterator.remove();
                if(null!=call&&!call.isCanceled()){
                    builder.append("Call:"+call.isExecuted()+" isCanceled:"+call.isCanceled()+":"+call.toString()+"<#>");
                    call.cancel();
                }
            }
            HttpLog.d("Request cancel-result:"+tag+" "+builder.toString());
        }
    }


    public Observable<HttpResponse> call(final String tag,final RequestItem item, final HashMap<String,String> params){
        return Observable.create(new Observable.OnSubscribe<HttpResponse>() {
            @Override
            public void call(Subscriber<? super HttpResponse> subscriber) {
                Call call =null;
                String result=null;
                String requestUrl=null;
                boolean isSuccessful=false;
                boolean requestFail=false;
                Map<String,String> headerItems=new HashMap<>();
                try {
                    long st = System.currentTimeMillis();
                    final Request request = getRequest(tag, item, params);
                    call = httpClient.newCall(request);
                    List<Call> calls = callItems.get(tag);
                    if(null==calls){
                        callItems.put(tag,calls=new ArrayList<>());
                    }
                    calls.add(call);
                    Response response = call.execute();
                    Headers headers = request.headers();
                    if (null != headers&&0<headers.size()) {
                        Set<String> names = headers.names();
                        for (String item : names) {
                            headerItems.put(item, headers.get(item));
                        }
                    }
                    //url headers
                    result = response.body().string();
                    requestUrl=request.url().toString();
                    isSuccessful=response.isSuccessful();
                    item.useTime=System.currentTimeMillis()-st;
                } catch(Exception e){
                    //request failed
                    requestFail=true;
                    removeCall(tag,call);
                    HttpException exception = new HttpException();
                    exception.code = IRequest.REQUEST_ERROR;
                    exception.message = e.getMessage();
                    if(null!=requestConfig.requestResultListener){
                        requestConfig.requestResultListener.onFailed(exception,item,item.url);
                    }
                    subscriber.onError(exception);
                    HttpLog.d("Request error:"+item.info+" Error:"+e.getMessage());
                }
                removeCall(tag,call);
                HttpResponse httpResponse=new HttpResponse();
                if (isSuccessful) {
                    httpResponse.result = result;
                    subscriber.onNext(httpResponse);
                    if(null!=requestConfig.requestResultListener){
                        requestConfig.requestResultListener.onSuccess(httpResponse,item,requestUrl);
                    }
                    HttpLog.d("Request success:"+item.info);
                }else if(!requestFail){
                    //request success but content is fail
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
                        requestConfig.requestResultListener.onFailed(exception, item,requestUrl);
                    }
                    subscriber.onError(exception);
                    HttpLog.d("Request failed:"+item.info+" Message:"+exception.message+" code:"+exception.code);
                }
                subscriber.onCompleted();
            }

        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * ensure request item,run on thread
     * @param item
     */
    private void ensureRequestItems(RequestItem item) {
        //ensure xml config item
        if(!TextUtils.isEmpty(item.action)){
            if(cacheItems.isEmpty()){
                XmlElement rootElement =null;
                if(!TextUtils.isEmpty(requestConfig.path)){
                    rootElement = configReader.readXmlElement(XmlReaderBase.ASSET_XML, requestConfig.path);
                } else if(!TextUtils.isEmpty(requestConfig.rawName)){
                    rootElement = configReader.readXmlElement(XmlReaderBase.RAW_XML, requestConfig.rawName);
                }
                if(null!=rootElement){
                    HashMap<String, RequestItem> items = configReader.readXmlConfig(rootElement);
                    if(null!=items&&!items.isEmpty()){
                        cacheItems.clear();
                        cacheItems.putAll(items);
                    }
                }
            }
            RequestItem requestItem = cacheItems.get(item.action);
            if(null!=requestItem){
                item.method=requestItem.method;
                item.param=requestItem.param;
                item.url=requestItem.url;
                item.info=requestItem.info;
                HttpLog.d("Get request item,action:"+item.info);
            } else {
                HttpLog.d("Request failed: Not config action "+item.info+",please check!");
            }
        }
        if(TextUtils.isEmpty(item.url)){
            HttpLog.d("Request failed:request url is null!");
            throw new NullPointerException("request url is null!");
        } else if(!(TextUtils.isEmpty(item.method)||"get".equals(item.method))&&!"post".equals(item.method)&&!"put".equals(item.method)){
            HttpLog.d("Request failed:http request method error,not get post or put!");
            throw new IllegalArgumentException("http request method error,not get post or put!");
        }
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
                item.params.putAll(extraItems);
            }
        }
        String requestUrl = NetUtils.getRequestUrl(requestConfig,item);
        if(POST.equals(item.method)||PUT.equals(item.method)){
            RequestBody requestBody=null;
            if(null!=item.pathParams){
                requestUrl=String.format(requestUrl,item.pathParams);
            }
            if(null!=item.entity&&!TextUtils.isEmpty(item.entity.second)){
                requestBody=RequestBody.create(MediaType.parse(item.entity.first), item.entity.second);
                HttpLog.d("Request entity:"+requestUrl+"\nmediaType:"+item.entity.first+"\nJson:\n"+item.entity);
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
                            requestBody=RequestBody.create(STREAM, file);
                            builder.addFormDataPart(name,file.getName(),requestBody);
                        }
                    }
                }
                requestBody=builder.build();
                HttpLog.d("Send Request:"+item.info+" Method:"+item.method+":"+requestUrl+" From:\n"+formParams);
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
            fullUrl.append(NetUtils.getParamValue(params));

            HttpLog.d("Get:" + fullUrl.toString());
            Request.Builder requestBuilder = new Request.Builder().url(fullUrl.toString());
            initRequestBuild(tag, item, requestBuilder);
            request=requestBuilder.build();
        }
        //add cookie
        if (null!=item.cookies&&!item.cookies.isEmpty()) {
            Request.Builder newBuilder = request.newBuilder();
            String cookieValue = NetUtils.getCookieValue(item.cookies);
            newBuilder.header("Cookie", cookieValue);
            HttpLog.d("Request add cookie:" + cookieValue);
            request=newBuilder.build();
        }
        return request;
    }


    private void initRequestBuild(String tag, RequestItem item, Request.Builder requestBuilder) {
        StringBuilder headerBuilder = new StringBuilder();
        HashMap<String, String> headers = new HashMap<>();
        //add global header items
        if(null!=requestConfig&&null!=requestConfig.listener){
            HashMap<String, String> headerItems = requestConfig.listener.requestHeaderItems();
            if(null!=headerItems && !headerItems.isEmpty()){
                headers.putAll(headerItems);
            }
        }
        //add custom header items
        if(null!=item.headers&&!item.headers.isEmpty()){
            headers.putAll(item.headers);
        }

        if(null!=headers&&!headers.isEmpty()){
            for(Map.Entry<String,String> entry:headers.entrySet()){
                //过滤掉Header值为空的情况
                if(!TextUtils.isEmpty(entry.getValue())) {
                    headerBuilder.append(entry.getKey() + "=" + entry.getValue() + ";");
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
        if(null!=tag){
            requestBuilder.tag(tag);
        }
        HttpLog.d(item.info+" header:"+headerBuilder.toString());
    }


}
