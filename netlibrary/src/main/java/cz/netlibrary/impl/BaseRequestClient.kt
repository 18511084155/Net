package cz.netlibrary.impl

import android.util.Patterns
import cz.netlibrary.callback.RequestCallback
import cz.netlibrary.configradtion.HttpRequestConfig
import cz.netlibrary.model.RequestConfig
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by cz on 2017/6/7.
 */
abstract class BaseRequestClient<out T> {

    companion object {
        val httpClient: OkHttpClient
        var requestConfig: HttpRequestConfig = HttpRequestConfig()
        init {
            val interceptor = Interceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                originalResponse.newBuilder().removeHeader("Pragma").header("Cache-Control", String.format("max-age=%d", 10)).build()
            }
            val clientBuilder = OkHttpClient.Builder()
                    .connectTimeout(requestConfig.connectTimeout.toLong(), TimeUnit.SECONDS)
                    .readTimeout(requestConfig.readTimeout.toLong(), TimeUnit.SECONDS)
                    .writeTimeout(requestConfig.writeTimeout.toLong(), TimeUnit.SECONDS)
                    .retryOnConnectionFailure(requestConfig.retryOnConnectionFailure)
                    .addNetworkInterceptor(interceptor)
            val cachedFile = requestConfig.cachedFile
            if (null != cachedFile && cachedFile.exists()) {
                val maxCacheSize = requestConfig.maxCacheSize
                clientBuilder.cache(Cache(cachedFile, maxCacheSize))
            }
            httpClient = clientBuilder.build()
        }
    }

    /**
     * 请求网络
     * @param tag 为结束任务tag
     * @param item 请求信息体
     * @return HttpResponse 请求返回结果
     *
     */
    abstract fun call(tag:String,item: RequestConfig,callback:RequestCallback<T>?)

    /**
     * 框架终止一个正在请求中的网络
     */
    abstract fun cancel(tag: String)

    /**
     * 庳展RequestConfig,获取完整的配置url
     */
    fun RequestConfig.getRequestUrl(): String {
        //此设计在应用requestItem之前,可以全局拦截,修改信息
        val requestUrl = requestConfig.pre?.invoke(this)
        val absoluteUrl: String
        if(null!=requestUrl){
            if(!Patterns.WEB_URL.matcher(requestUrl).matches()){
                throw IllegalArgumentException("error $requestUrl when call pre!")
            } else {
                absoluteUrl=requestUrl
            }
        } else if (!url.startsWith("http")) {
            absoluteUrl = requestConfig.url+url
        } else {
            absoluteUrl = url
        }
        return absoluteUrl
    }


}