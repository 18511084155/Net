package xyqb.net;

import java.io.File;

import xyqb.net.callback.OnApplyRequestItemListener;
import xyqb.net.callback.OnClientCreateCallback;
import xyqb.net.callback.OnPrintHttpLogListener;
import xyqb.net.callback.OnRequestListener;
import xyqb.net.callback.OnRequestResultListener;
import xyqb.net.log.HttpLog;
import xyqb.net.model.RequestConfig;

/**
 * Created by cz on 8/23/16.
 */
public class NetManager {
    private final int MAX_TIME_OUT=8*1000;
    private final int MAX_CACHE_SIZE=4*1024*1024;
    private static NetManager instance=new NetManager();
    private final RequestConfig requestConfig=new RequestConfig();

    private NetManager(){
        requestConfig.connectTimeout=MAX_TIME_OUT;
        requestConfig.writeTimeout=MAX_TIME_OUT;
        requestConfig.readTimeout=MAX_TIME_OUT;
        requestConfig.maxCacheSize=MAX_CACHE_SIZE;
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

    public NetManager setOnApplyRequestItemListener(OnApplyRequestItemListener listener){
        this.requestConfig.applyListener=listener;
        return this;
    }

    public NetManager setOnRequestResultListener(OnRequestResultListener requestResultListener){
        this.requestConfig.requestResultListener=requestResultListener;
        return this;
    }

    public NetManager setOnClientCreateCallback(OnClientCreateCallback clientCreateCallback){
        this.requestConfig.clientCreateCallback=clientCreateCallback;
        return this;
    }

    public NetManager setDebug(boolean debug){
        HttpLog.setDebug(debug);
        return this;
    }

    public NetManager setOnPrintHttpLogListener(OnPrintHttpLogListener listener){
        HttpLog.setPrintHttpLogListener(listener);
        return this;
    }

    public RequestConfig getRequestConfig(){
        return requestConfig;
    }

}
