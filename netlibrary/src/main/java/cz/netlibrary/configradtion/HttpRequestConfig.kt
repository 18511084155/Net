package cz.netlibrary.configradtion

import cz.netlibrary.exception.HttpException
import cz.netlibrary.model.RequestConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File

/**
 * Created by cz on 2017/6/7.
 */
class HttpRequestConfig {
    var httpLog=false//打印网络信息
    var abortOnError=false //运行异常是否终止
    var url: String? = null
    var errorMessage:String?=null
    var connectTimeout: Int = 16*1000
    var readTimeout: Int = 16*1000
    var writeTimeout: Int = 16*1000
    var cachedFile: File? = null //缓存目录
    var maxCacheSize: Long = 10*1024*1024 //最大缓存信息
    var retryOnConnectionFailure=false //异常重试
    var interceptItems:Array<Interceptor>?=null//额外的拦截器
    internal var clientCreateCallback:((OkHttpClient.Builder)->Unit)?=null
    internal var requestExtrasCallback:(()->MutableMap<String,String>)?=null //附加参数
    internal var requestHeaderCallback:(()->MutableMap<String,String>)?=null //附加头信息
    internal var requestErrorCallback:((Int,String?,String?)->HttpException)?=null
    internal var networkInterceptor:(RequestConfig.()->Boolean)?=null

    fun clientCreateCallback(callback:(OkHttpClient.Builder)->Unit){
        this.clientCreateCallback=callback
    }
    fun requestExtrasCallback(callback:()->MutableMap<String,String>){
        this.requestExtrasCallback =callback
    }
    fun requestHeaderCallback(callback:()->MutableMap<String,String>){
        this.requestHeaderCallback =callback
    }
    //网络拦截器
    fun networkInterceptor(interceptor:RequestConfig.()->Boolean){ networkInterceptor =interceptor }
    //异常数据处理器
    fun requestErrorCallback(action:(Int,String?,String?)->HttpException){ requestErrorCallback=action}

}