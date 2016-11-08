package xyqb.net;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import xyqb.net.callback.OnRequestFailedListener;
import xyqb.net.callback.OnRequestSuccessListener;
import xyqb.net.callback.OnResultCacheListener;
import xyqb.net.exception.HttpException;
import xyqb.net.impl.OKHttp3;
import xyqb.net.log.HttpLog;
import xyqb.net.model.HttpResponse;
import xyqb.net.model.RequestItem;
import xyqb.net.resultfilter.ResultFilter;

/**
 * Created by cz on 8/23/16.
 */
public class HttpRequest<T> {
    public static final String MEDIATYPE_JSON="application/json; charset=utf-8";
    private static final IRequest requester =new OKHttp3();
    private static final HashMap<String,List<Subscription>> subscriptionItems;
    private static Context appContext;
    private OnResultCacheListener resultCacheListener;
    private OnRequestSuccessListener successListener;
    private OnRequestFailedListener failedListener;
    private ResultFilter<T> requestFilter;
    private RequestItem requestItem;
    private Object[] params;

    static {
        appContext = getContext();
        subscriptionItems=new HashMap<>();
    }

    public static Context getContext() {
        if (appContext == null) {
            try {
                Application application=(Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
                appContext = application.getApplicationContext();
            } catch (final Exception e) {
            }
        }
        return appContext;
    }


    public static HttpRequest obtain(String action,final Object...params){
        return new HttpRequest(action,params);
    }

    public HttpRequest url(String url){
        requestItem.dynamicUrl=url;
        return this;
    }

    private HttpRequest(String action,Object... params){
        requestItem=new RequestItem();
        requestItem.action=action;
        this.params=params;
    }

    public HttpRequest addHeader(String name,String value){
        requestItem.headers.put(name, value);
        return this;
    }

    public HttpRequest addCookie(String name,String value){
        requestItem.cookies.put(name, value);
        return this;
    }

    public HttpRequest addPathValue(Object... params){
        requestItem.pathParams=params;
        return this;
    }

    public HttpRequest addParams(String name,String value){
        requestItem.params.put(name,value);
        return this;
    }

    public HttpRequest addParams(Map<String,String> params){
        if(null!=params){
            requestItem.params.putAll(params);
        }
        return this;
    }

    public HttpRequest addPart(String name,File file){
        requestItem.partBody.put(name,file);
        return this;
    }

    public HttpRequest addStringEntity(String value){
        requestItem.entity=new Pair<>(MEDIATYPE_JSON,value);
        return this;
    }

    public HttpRequest addStringEntity(String mediaType,String value){
        requestItem.entity=new Pair<>(mediaType,value);
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


    public void call(){
        final String tag=getCallClassTag();
        request(tag);
    }

    private  String getCallClassTag() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int length = stackTrace.length;
        String name = getClass().getName();
        StackTraceElement stackTraceElement = null;
        boolean isLogClass=false;
        for (int i = 0; i < length; i++) {
            stackTraceElement = stackTrace[i];
            if (name.equals(stackTraceElement.getClassName())) {
                isLogClass=true;
            } else if(isLogClass){
                break;
            }
        }
        String className=stackTraceElement.getClassName();
        int index = className.indexOf("$");
        if(-1<index){
            className=className.substring(0,index);
        }
        return className;

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
        Observable<HttpResponse> observable = requester.call(tag, requestItem, params);
        if(isEnableNetWork()){
            Subscription subscribe = observable.map(new Func1<HttpResponse, Pair<HttpResponse, T>>() {
                @Override
                public Pair<HttpResponse, T> call(HttpResponse response) {
                    T t;
                    if (null != requestFilter) {
                        t = requestFilter.result(response.result);
                        HttpLog.d("Result filter complete, The object is:" + t);
                    } else {
                        t = (T) response.result;
                    }
                    //请求结果处理
                    if (null != resultCacheListener) {
                        resultCacheListener.onResultCache(response.result);
                    }
                    return new Pair<>(response, t);
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
                            exception = (HttpException) throwable;
                            HttpLog.d("Request failed code:" + exception.code + " message:\n" + exception.message);
                        } else {
                            exception = new HttpException();
                            exception.message = throwable.getMessage();
                            exception.code = IRequest.REQUEST_ERROR;
                            HttpLog.d("Request failed:\n" + exception.getMessage());
                        }
                        failedListener.onFailed(exception.code, exception);
                    }
                }
            });
            List<Subscription> subscriptions = subscriptionItems.get(tag);
            if(null==subscriptions){
                subscriptionItems.put(tag,subscriptions=new ArrayList<>());
            }
            subscriptions.add(subscribe);
        } else if(null!=failedListener){
            HttpException exception=new HttpException();
            exception.code=IRequest.REQUEST_NO_NETWORK;
            exception.message="Request no network!";
            HttpLog.d("Request failed:\n"+exception.getMessage());
            failedListener.onFailed(exception.code, exception);
        }
    }

    public static void cancel(Object object){
        String tag = object.getClass().getName();
        requester.cancel(tag);
        List<Subscription> subscriptions = subscriptionItems.get(tag);
        if(null!=subscriptions){
            for(Iterator<Subscription> iterator=subscriptions.iterator();iterator.hasNext();){
                Subscription subscription = iterator.next();
                iterator.remove();
                if(subscription.isUnsubscribed()){
                    subscription.unsubscribe();
                }
            }
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

        public Builder addParams(Map<String,String> params){
            if(null!=params){
                requestItem.params.putAll(params);
            }
            return this;
        }

        public Builder addHeader(String name,String value){
            requestItem.headers.put(name,value);
            return this;
        }

        public Builder addCookie(String name,String value){
            requestItem.cookies.put(name, value);
            return this;
        }

        public Builder addPart(String name,File file){
            requestItem.partBody.put(name,file);
            return this;
        }

        public Builder addStringEntity(String value){
            requestItem.entity=new Pair<>(MEDIATYPE_JSON,value);
            return this;
        }

        public Builder addStringEntity(String mediaType,String value){
            requestItem.entity=new Pair<>(mediaType,value);
            return this;
        }

        public HttpRequest build(){
            HttpRequest httpRequest = new HttpRequest(null);
            httpRequest.requestItem=requestItem;
            return httpRequest;
        }
    }


}
