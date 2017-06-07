package cz.netlibrary

import android.app.Activity
import android.app.Application
import android.app.Dialog
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

fun<T> getRequestItem(action:String?,request: RequestBuilder<T>.()->Unit):Pair<RequestConfig, RequestHandler<T>>{
    var requestItem: RequestItem? =null
    if(null!=action){
        requestItem = Configuration[action]
    }
    val requestBuilder = RequestBuilder<T>().apply(request)
    val config = requestBuilder.config
    val handler = requestBuilder.handler
    handler.mainThread=requestBuilder.mainThread
    if(null==requestItem||!config.init){
        throw IllegalAccessException("Must use a template config or a get/post to request!")
    } else if(null!=requestItem){
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
 * v4 activity
 */
fun<T> Activity.request(action:String?=null, request: RequestBuilder<T>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(this.javaClass.simpleName, item, handler)
}

/**
 * v4 activity request string
 */
fun Activity.requestString(action:String?=null, request: RequestBuilder<String>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(this.javaClass.simpleName, item, handler)
}

/**
 * v4 fragment
 */
fun<T> Fragment.request(action:String?=null, request: RequestBuilder<T>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}

/**
 * v4 fragment request string
 */
fun Fragment.requestString(action:String?=null, request: RequestBuilder<String>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}

/**
 * app activity
 */
fun<T> android.app.Fragment.request(action:String?=null,request: RequestBuilder<T>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}

/**
 * app activity request string
 */
fun android.app.Fragment.requestString(action:String?=null,request: RequestBuilder<String>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}

/**
 * v4 dialogFragment
 */
fun<T> DialogFragment.request(action:String?=null, request: RequestBuilder<T>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}

/**
 * v4 dialogFragment request string
 */
fun DialogFragment.requestString(action:String?=null, request: RequestBuilder<String>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}

/**
 * app dialogFragment
 */
fun<T> android.app.DialogFragment.request(action:String?=null,request: RequestBuilder<T>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}

/**
 * app dialogFragment request string
 */
fun android.app.DialogFragment.requestString(action:String?=null,request: RequestBuilder<String>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}

/**
 * app dialog
 */
fun<T> Dialog.request(action:String?=null, request: RequestBuilder<T>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}

/**
 * app dialog request string
 */
fun Dialog.requestString(action:String?=null, request: RequestBuilder<String>.()->Unit){
    val (item,handler) = getRequestItem(action, request)
    RequestClient.request(javaClass.simpleName, item, handler)
}
