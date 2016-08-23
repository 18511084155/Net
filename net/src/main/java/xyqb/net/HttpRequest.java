package xyqb.net;

import android.text.TextUtils;

import rx.Observable;
import rx.functions.Action1;
import xyqb.net.callback.OnRequestFailedListener;
import xyqb.net.callback.OnRequestSuccessListener;
import xyqb.net.exception.HttpException;
import xyqb.net.impl.OKHttp3;
import xyqb.net.model.HttpResponse;
import xyqb.net.model.RequestItem;
import xyqb.net.resultfilter.ResultFilter;

/**
 * Created by cz on 8/23/16.
 */
public class HttpRequest {
    private static final IRequest requester =new OKHttp3();
    private OnRequestSuccessListener successListener;
    private OnRequestFailedListener failedListener;
    private RequestItem requestItem;
    private Object[] params;

    public static HttpRequest call(){
        return new HttpRequest();
    }

    public static HttpRequest request(String action,Object...params){
        HttpRequest httpRequest = new HttpRequest(params);
        httpRequest.requestItem = NetManager.getInstance().getRequestItem(action);
        if(null==httpRequest.requestItem){
            throw new NullPointerException("No config action info!");
        }
        return httpRequest;
    }

    private HttpRequest(Object... params){
        this.params=params;
    }

    public HttpRequest addHeader(String name,String value){
        requestItem.headers.put(name, value);
        return this;
    }

    public HttpRequest setOnRequestSuccessListener(OnRequestSuccessListener listener){
        this.successListener=listener;
        return this;
    }

    public HttpRequest setOnRequestFailedListener(OnRequestFailedListener listener){
        this.failedListener=listener;
        return this;
    }

    public void setResult(ResultFilter resultFilter){
        this.requester.setResultFilter(resultFilter);
    }


    public void call(Object obj){
        ensureRequestItem();
        Observable<HttpResponse> observable = requester.call(obj, requestItem, params);
        observable.subscribe(new Action1<HttpResponse>() {
            @Override
            public void call(HttpResponse response) {
                if (null != successListener) {
                    successListener.onSuccess(response);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                if (null != failedListener&&throwable instanceof HttpException) {
                    failedListener.onFailed((HttpException) throwable);
                }
            }
        });
    }

    private void ensureRequestItem(){
        if(TextUtils.isEmpty(requestItem.url)){
            throw new NullPointerException("request url is null!");
        } else if("get".equals(requestItem.method)&&"post".equals(requestItem.method)){
            throw new IllegalArgumentException("http request method error,not get or post!");
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

        public HttpRequest build(){
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.requestItem=requestItem;
            return httpRequest;
        }
    }


}
