package cz.netlibrary

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import cz.netlibrary.configradtion.HttpRequestConfig
import cz.netlibrary.impl.BaseRequestClient
import cz.netlibrary.model.Configuration
import cz.netlibrary.model.RequestConfig
import cz.netlibrary.model.RequestItem
import cz.netlibrary.request.RequestBuilder
import cz.netlibrary.request.RequestClient
import cz.netlibrary.request.RequestHandler

/**
 * Created by cz on 2017/6/7.
 * activity/fragment扩展网络操作,以及配置
 */
fun Application.init(closure: HttpRequestConfig.()->Unit){
    //配置的全局网格信息
    BaseRequestClient.requestConfig = HttpRequestConfig().apply(closure)
}

fun<T> getRequestItem(action:String?,request: RequestBuilder<T>.()->Unit): Pair<RequestConfig, RequestHandler<T>> {
    var requestItem: RequestItem? =null
    if(null!=action){
        requestItem = Configuration[action]
    }
    val requestBuilder = RequestBuilder<T>().apply(request)
    val config = requestBuilder.config
    val handler = requestBuilder.handler
    handler.mainThread=requestBuilder.mainThread
    handler.contextCondition=requestBuilder.contextCondition
    if(null!=requestItem){
        //请求网络
        config.info=requestItem.info
        config.url=requestItem.url
        config.method=requestItem.method
        config.info=requestItem.info
        config.templateName=requestItem.params
        config.pathValues.addAll(requestItem.pathValues)
    }
    return config.to(handler)
}

/**
 * activity
 */
fun<T> Activity.request(tag:String?=null,action:String?=null, request: RequestBuilder<T>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(getAnyTag(tag,this), item, handler){
        val condition=if(Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN_MR1)
            !isFinishing
         else
            !isFinishing||!isDestroyed
        !handler.contextCondition||condition
    }
}

fun<T> Activity.request(action:String?=null, request: RequestBuilder<T>.()->Unit)=request(null,action,request)

/**
 * activity request string
 */
fun Activity.requestString(action:String?=null, request: RequestBuilder<String>.()->Unit){
    request(action,request)
}

fun Activity.cancelRequest(tag:String?=null)=RequestClient.cancel(tag,this)

/**
 * v4 fragment
 */
fun<T> Fragment.request(tag:String?=null,action:String?=null, request: RequestBuilder<T>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(getAnyTag(tag,this), item, handler){ !handler.contextCondition||!isDetached&&null!=view?.windowToken }
}

fun<T> Fragment.request(action:String?=null, request: RequestBuilder<T>.()->Unit):Unit=request(action,request)
/**
 * v4 fragment request string
 */
fun Fragment.requestString(action:String?=null, request: RequestBuilder<String>.()->Unit){
    request(action,request)
}

fun Fragment.cancelRequest(tag:String?=null)=RequestClient.cancel(tag,this)

/**
 * v4 dialogFragment
 */
fun<T> DialogFragment.request(tag:String?=null,action:String?=null, request: RequestBuilder<T>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(getAnyTag(tag,this), item, handler){!handler.contextCondition||!isDetached&&null!=view?.windowToken}
}

fun<T> DialogFragment.request(action:String?=null, request: RequestBuilder<T>.()->Unit):Unit=request(action,request)
/**
 * v4 dialogFragment request string
 */
fun DialogFragment.requestString(action:String?=null, request: RequestBuilder<String>.()->Unit)=request(action,request)

fun DialogFragment.cancelRequest(tag:String?=null)=RequestClient.cancel(tag,this)


fun getAnyTag(tag:String?=null,any:Any):String=if(null!=tag) any.javaClass.simpleName+tag else any.javaClass.simpleName



//----------------------------------------------------
//网络块扩展
//----------------------------------------------------
fun Activity.enableNetWork():Boolean=enableNetWork(this)
fun Activity.isWifi():Boolean=isWifi(this)
fun Activity.isMobile():Boolean=isMobile(this)

fun Fragment.enableNetWork():Boolean=enableNetWork(context)
fun Fragment.isWifi():Boolean=isWifi(context)
fun Fragment.isMobile():Boolean=isMobile(context)


fun enableNetWork(context:Context?): Boolean {
    val context=context?:return false
    return isWifi(context) || isMobile(context)
}

fun isWifi(context:Context?): Boolean {
    val context=context?:return false
    var result:Boolean=false
    val systemService = context.getSystemService(Context.CONNECTIVITY_SERVICE)
    if(systemService is ConnectivityManager){
        result=systemService.activeNetworkInfo.type==ConnectivityManager.TYPE_WIFI
    }
    return result
}

fun isMobile(context:Context?): Boolean {
    val context=context?:return false
    var result:Boolean=false
    val systemService = context.getSystemService(Context.CONNECTIVITY_SERVICE)
    if(systemService is ConnectivityManager){
        result=systemService.activeNetworkInfo.type==ConnectivityManager.TYPE_MOBILE
    }
    return result
}