package cz.netlibrary.configradtion

import cz.netlibrary.exception.HttpException
import cz.netlibrary.model.RequestConfig
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
    internal var requestExtrasCallback:(()->MutableMap<String,String>)?=null //附加参数
    internal var requestHeaderCallback:(()->MutableMap<String,String>)?=null //附加头信息
    internal var requestSuccessCallback:((String)->String)?=null
    internal var requestErrorCallback:((Int,String)->HttpException)?=null
    internal var requestConditionCallback:((String)->Boolean)?=null
    internal var applyRequest:(RequestConfig.()->RequestConfig)?=null
    internal var networkInterceptor:(RequestConfig.()->Boolean)?=null
    internal var requestCallback:((String?, Int, HttpException?)->Unit)?=null

    fun requestExtrasCallback(callback:()->MutableMap<String,String>){
        this.requestExtrasCallback =callback
    }
    fun requestHeaderCallback(callback:()->MutableMap<String,String>){
        this.requestHeaderCallback =callback
    }
    fun applyRequest(action:RequestConfig.()->RequestConfig){ this.applyRequest =action }
    //网络拦截器
    fun networkInterceptor(interceptor:RequestConfig.()->Boolean){ networkInterceptor =interceptor }
    //请求回调器
    fun requestCallback(action:(String?,Int,HttpException?)->Unit){ requestCallback =action }
    //自定义请求成功校验器,用于服务器返回200,但实现值为空或者返回code不对,此处中转校验
    fun requestConditionCallback(action:(String)->Boolean){ this.requestConditionCallback=action }

    fun requestSuccessCallback(action:(String)->String){
        this.requestSuccessCallback=action
    }
    //异常数据处理器
    fun requestErrorCallback(action:(Int,String)->HttpException){ requestErrorCallback=action}

}