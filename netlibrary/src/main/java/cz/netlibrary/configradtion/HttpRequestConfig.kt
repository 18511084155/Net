package cz.netlibrary.configradtion

import cz.netlibrary.exception.HttpException
import cz.netlibrary.model.RequestConfig
import java.io.File

/**
 * Created by cz on 2017/6/7.
 */
class HttpRequestConfig {
    var abortOnError=false //运行异常是否终止
    var url: String? = null
    var connectTimeout: Int = 16*1000
    var readTimeout: Int = 16*1000
    var writeTimeout: Int = 16*1000
    var cachedFile: File? = null //缓存目录
    var maxCacheSize: Long = 10*1024*1024 //最大缓存信息
    var retryOnConnectionFailure=false //异常重试
    var extrasParams:Map<String,String>?=null //附加参数
    var extrasHeader:Map<String,String>?=null //附加头信息
    var requestErrorCallback:((Int,String)->HttpException)?=null
    var applyRequest:(RequestConfig.()->RequestConfig)?=null
    var networkInterceptor:(RequestConfig.()->Boolean)?=null
    var requestCallback:((String?, Int, HttpException?)->Unit)?=null
    var httpLog=false//打印网络信息
    fun applyRequest(action:RequestConfig.()->RequestConfig){ this.applyRequest =action }
    //网络拦截器
    fun networkInterceptor(interceptor:RequestConfig.()->Boolean){ networkInterceptor =interceptor }
    //请求回调器
    fun requestCallback(action:(String?,Int,HttpException?)->Unit){ requestCallback =action }

}