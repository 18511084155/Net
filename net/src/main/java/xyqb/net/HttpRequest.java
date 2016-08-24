package xyqb.net;

import android.text.TextUtils;
import android.util.Pair;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
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
public class HttpRequest<T> {
    private static final IRequest requester =new OKHttp3();
    private OnRequestSuccessListener successListener;
    private OnRequestFailedListener failedListener;
    private ResultFilter<T> requestFilter;
    private RequestItem requestItem;
    private String action;
    private Object[] params;


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
                    item.dynamicUrl = requestItem.dynamicUrl;
                    item.headers = requestItem.headers;
                    requestItem = item;
                    request(tag);
                }
            });
        } else {
            request(tag);
        }
    }

    private void request(String tag) {
        ensureRequestItem(requestItem);
        Observable<HttpResponse> observable = requester.call(tag, requestItem, params);
        observable.map(new Func1<HttpResponse,Pair<HttpResponse,T>>() {
            @Override
            public Pair<HttpResponse, T> call(HttpResponse response) {
                T t;
                if(null!=HttpRequest.this.requestFilter){
                    t = HttpRequest.this.requestFilter.result(response.result);
                } else {
                    t= (T) response.result;
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
                    } else {
                        exception=new HttpException();
                        exception.message=throwable.getMessage();
                        exception.code=IRequest.REQUEST_ERROR;
                    }
                    failedListener.onFailed(exception.code, exception);
                }
            }
        });
    }

    private void ensureRequestItem(RequestItem item){
        if(null==item){
            throw new NullPointerException("request item is null!");
        } else if(TextUtils.isEmpty(item.url)){
            throw new NullPointerException("request url is null!");
        } else if(!(TextUtils.isEmpty(item.method)||"get".equals(item.method))&&!"post".equals(item.method)){
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
