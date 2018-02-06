package cz.netlibrary.impl

import android.util.Patterns
import cz.netlibrary.callback.RequestCallback
import cz.netlibrary.configradtion.HttpRequestConfig
import cz.netlibrary.model.RequestConfig
import cz.netlibrary.requestConfig
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * Created by cz on 2017/6/7.
 */
abstract class BaseRequestClient<out T,C> {

    companion object {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val TEXT = MediaType.parse("Content-Type application/x-www-form-")
        val STREAM = MediaType.parse("application/octet-stream")
        val httpClient: OkHttpClient
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
            //添加自定义一些配置
            requestConfig.clientCreateCallback?.invoke(clientBuilder)
            //添加额外的拦截器
            requestConfig.interceptItems?.forEach{clientBuilder.addInterceptor(it)}
            //配置缓存目录
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

    abstract fun syncCall(tag:String,item: RequestConfig,callback:RequestCallback<T>?):T?

    /**
     * 获得请求对象
     */
    abstract fun getHttpClient():C

    /**
     * 框架终止一个正在请求中的网络
     */
    abstract fun cancel(tag: String)

    /**
     * 庳展RequestConfig,获取完整的配置url
     */
    fun RequestConfig.getRequestUrl(): String =if (!url.startsWith("http")) requestConfig.url+url else  url


}