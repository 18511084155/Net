package xyqb.net;

import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import xyqb.net.callback.OnRequestListener;
import xyqb.net.callback.OnRequestResultListener;
import xyqb.net.log.HttpLog;
import xyqb.net.model.RequestConfig;
import xyqb.net.model.RequestItem;
import xyqb.net.xml.RequestConfigReader;

/**
 * Created by cz on 8/23/16.
 */
public class NetManager {
    private final int MAX_TIME_OUT=8*1000;
    private final int MAX_CACHE_SIZE=4*1024*1024;
    private static NetManager instance=new NetManager();
    private final RequestConfig requestConfig=new RequestConfig();
    private final RequestConfigReader configReader;
    private final HashMap<String,RequestItem> cacheItems;

    private NetManager(){
        cacheItems =new HashMap<>();
        requestConfig.connectTimeout=MAX_TIME_OUT;
        requestConfig.writeTimeout=MAX_TIME_OUT;
        requestConfig.readTimeout=MAX_TIME_OUT;
        requestConfig.maxCacheSize=MAX_CACHE_SIZE;
        configReader=new RequestConfigReader(requestConfig);
    }

    public static NetManager getInstance(){
        return instance;
    }

    public NetManager setRawName(String rawId){
        requestConfig.rawName=rawId;
        return this;
    }
    public NetManager setConfigPath(String path){
        requestConfig.path=path;
        return this;
    }

    public NetManager setRequestUrl(String url){
        requestConfig.url=url;
        return this;
    }

    public NetManager setHttpDebug(boolean debug){
        HttpLog.setHttpDebug(debug);
        return this;
    }

    public NetManager setConnectTimeout(int second){
        this.requestConfig.connectTimeout=second;
        return this;
    }

    public NetManager setReadTimeout(int second){
        this.requestConfig.readTimeout=second;
        return this;
    }

    public NetManager setWriteTimeout(int second){
        this.requestConfig.writeTimeout=second;
        return this;
    }

    public NetManager setCacheFile(File cacheFile){
        this.requestConfig.cachedFile=cacheFile;
        return this;
    }

    public NetManager setMaxCacheSize(long maxCacheSize){
        this.requestConfig.maxCacheSize=maxCacheSize;
        return this;
    }

    public NetManager setRetryOnConnectionFailure(boolean retryOnConnectionFailure){
        this.requestConfig.retryOnConnectionFailure=retryOnConnectionFailure;
        return this;
    }

    public NetManager setOnRequestListener(OnRequestListener listener){
        this.requestConfig.listener=listener;
        return this;
    }

    public NetManager setOnRequestResultListener(OnRequestResultListener requestResultListener){
        this.requestConfig.requestResultListener=requestResultListener;
        return this;
    }

    public synchronized void requestItem(final String action,final Action1<RequestItem> callAction){
        if(null==callAction||TextUtils.isEmpty(action)) return;
        if(cacheItems.isEmpty()){
            Observable.create(new Observable.OnSubscribe<HashMap<String, RequestItem>>() {
                @Override
                public void call(Subscriber<? super HashMap<String, RequestItem>> subscriber) {
                    HashMap<String, RequestItem> items = configReader.readerRequestItems();
                    if (null != items) {
                        cacheItems.putAll(items);
                        subscriber.onNext(items);
                        subscriber.onCompleted();
                    }
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<HashMap<String, RequestItem>>() {
                @Override
                public void call(HashMap<String, RequestItem> items) {
                    //main
                    callAction.call(cacheItems.get(action));
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        } else {
            callAction.call(cacheItems.get(action));
        }
    }

    public RequestConfig getRequestConfig(){
        return requestConfig;
    }

}
