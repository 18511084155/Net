package xyqb.net;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Pair;

import java.io.File;
import java.lang.reflect.Method;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import xyqb.net.callback.OnRequestFailedListener;
import xyqb.net.callback.OnRequestSuccessListener;
import xyqb.net.callback.OnResultCacheListener;
import xyqb.net.exception.HttpException;
import xyqb.net.impl.OKHttp3;
import xyqb.net.model.HttpResponse;
import xyqb.net.model.RequestItem;
import xyqb.net.resultfilter.ResultFilter;

/**
 * Created by cz on 8/23/16.
 */
public class HttpRequest<T> {
    private static final IRequest requester =new OKHttp3();
    private static Context appContext;
    private OnResultCacheListener resultCacheListener;
    private OnRequestSuccessListener successListener;
    private OnRequestFailedListener failedListener;
    private ResultFilter<T> requestFilter;
    private RequestItem requestItem;
    private String action;
    private Object[] params;

    static {
        appContext = getContext();
    }


    public static Context getContext() {
        if (appContext == null) {
            try {
                final Class<?> activityThreadClass = HttpRequest.class.getClassLoader().loadClass("android.app.ActivityThread");
                final Method currentActivityThread = activityThreadClass
                        .getDeclaredMethod("currentActivityThread");
                final Object activityThread = currentActivityThread
                        .invoke(null);
                final Method getApplication = activityThreadClass
                        .getDeclaredMethod("getApplication");
                final Application application = (Application) getApplication
                        .invoke(activityThread);
                appContext = application.getApplicationContext();
            } catch (final Exception e) {
            }
        }
        return appContext;
    }


    public static HttpRequest obtain(String action,final Object...params){
        HttpRequest httpRequest = new HttpRequest(params);
        httpRequest.action=action;
        return httpRequest;
    }

    public HttpRequest url(String url){
        requestItem.dynamicUrl=url;
        return this;
    }

    private HttpRequest(Object... params){
        this.params=params;
        requestItem=new RequestItem();
    }

    public HttpRequest addHeader(String name,String value){
        requestItem.headers.put(name, value);
        return this;
    }

    public HttpRequest addPathValue(Object... params){
        requestItem.pathParams=params;
        return this;
    }

    public HttpRequest addPart(String name,File file){
        requestItem.partBody.put(name,file);
        return this;
    }

    public HttpRequest addStringEntity(String value){
        requestItem.entity=value;
        return this;
    }

    public HttpRequest setOnResultCacheListener(OnResultCacheListener listener){
        resultCacheListener=listener;
        return this;
    }

    public HttpRequest setOnRequestSuccessListener(OnRequestSuccessListener<T> listener){
        this.successListener=listener;
        return this;
    }

    public HttpRequest setOnRequestFailedListener(OnRequestFailedListener listener){
        this.failedListener=listener;
        return this;
    }

    public HttpRequest setResultFilter(ResultFilter<T> resultFilter){
        this.requestFilter=resultFilter;
        return this;
    }


    public void call(Object obj){
        final String tag=(null==obj?null:obj.getClass().getSimpleName());
        if(!TextUtils.isEmpty(action)){
            NetManager.getInstance().requestItem(action, new Action1<RequestItem>() {
                @Override
                public void call(RequestItem item) {
                    if(null!=item){
                        item.dynamicUrl = requestItem.dynamicUrl;
                        item.headers = requestItem.headers;
                        item.pathParams=requestItem.pathParams;
                        item.partBody=requestItem.partBody;
                        item.entity=requestItem.entity;
                        requestItem = item;
                        request(tag);
                        HttpLog.d("Get request item,call:"+action);
                    } else {
                        HttpLog.d("Not config action:"+action+",please check!");
                    }
                }
            });
        } else {
            request(tag);
        }
    }


    public static boolean isEnableNetWork() {
        return isWIFI() || isMobile();
    }


    public static boolean isWIFI() {
        Context context = getContext();
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean result = false;
        if (networkInfo != null) {
            result = networkInfo.isConnected();
        }
        return result;
    }


    public  static boolean isMobile() {
        Context context = getContext();
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean result = false;
        if (networkInfo != null) {
            result = networkInfo.isConnected();
        }
        return result;
    }

    private void request(String tag) {
        ensureRequestItem(requestItem);
        Observable<HttpResponse> observable = requester.call(tag, requestItem, params);
        if(isEnableNetWork()){
            observable.map(new Func1<HttpResponse,Pair<HttpResponse,T>>() {
                @Override
                public Pair<HttpResponse, T> call(HttpResponse response) {
                    T t;
                    if(null!=requestFilter){
                        t = requestFilter.result(response.result);
                        HttpLog.d("Result filter complete, The object is:"+t);
                    } else {
                        t= (T) response.result;
                    }
                    //请求结果处理
                    if(null!=resultCacheListener){
                        resultCacheListener.onResultCache(response.result);
                    }
                    return new Pair<>(response,t);
                }
            }).subscribe(new Action1<Pair<HttpResponse, T>>() {
                @Override
                public void call(Pair<HttpResponse, T> pair) {
                    if (null != successListener) {
                        successListener.onSuccess(pair.first, pair.second);
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    if (null != failedListener) {
                        HttpException exception;
                        if (throwable instanceof HttpException) {
                            exception=(HttpException)throwable;
                            HttpLog.d("Request failed code:"+exception.code+" message:\n"+exception.message);
                        } else {
                            exception=new HttpException();
                            exception.message=throwable.getMessage();
                            exception.code=IRequest.REQUEST_ERROR;
                            HttpLog.d("Request failed:\n"+exception.getMessage());
                        }
                        failedListener.onFailed(exception.code, exception);
                    }
                }
            });
        } else if(null!=failedListener){
            HttpException exception=new HttpException();
            exception.code=IRequest.REQUEST_NO_NETWORK;
            exception.message="Request no network!";
            HttpLog.d("Request failed:\n"+exception.getMessage());
            failedListener.onFailed(exception.code, exception);
        }
    }

    private void ensureRequestItem(RequestItem item){
        if(null==item){
            throw new NullPointerException("request item is null!");
        } else if(TextUtils.isEmpty(item.url)){
            throw new NullPointerException("request url is null!");
        } else if(!(TextUtils.isEmpty(item.method)||"get".equals(item.method))&&!"post".equals(item.method)&&!"put".equals(item.method)){
            throw new IllegalArgumentException("http request method error,not get post or put!");
        }
    }

    public static class Builder{
        private RequestItem requestItem;

        public Builder() {
            this.requestItem = new RequestItem();
        }

        public Builder url(String url){
            requestItem.url=url;
            return this;
        }

        public Builder method(String method){
            requestItem.method=method;
            return this;
        }

        public Builder addParams(String name,String value){
            requestItem.params.put(name,value);
            return this;
        }

        public Builder addHeader(String name,String value){
            requestItem.headers.put(name,value);
            return this;
        }

        public Builder addPart(String name,File file){
            requestItem.partBody.put(name,file);
            return this;
        }

        public Builder addStringEntity(String value){
            requestItem.entity=value;
            return this;
        }

        public HttpRequest build(){
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.requestItem=requestItem;
            return httpRequest;
        }
    }


}
