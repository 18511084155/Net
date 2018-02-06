package cz.netlibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import cz.netlibrary.configradtion.HttpRequestConfig
import cz.netlibrary.impl.BaseRequestClient
import cz.netlibrary.log.HttpLog
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
//操作失败
val REQUEST_FAILED=-2
val OPERATION_FAILED=-1
val JSON_MEDIA_TYPE:String="application/json; charset=utf-8"
internal var requestConfig = HttpRequestConfig()

fun Application.init(closure: HttpRequestConfig.()->Unit){
    //配置的全局网格信息
    requestConfig = HttpRequestConfig().apply(closure)
    //设置日志
    HttpLog.httpLog=requestConfig.httpLog
}


fun<T> getRequestItem(action:String?,request: RequestBuilder<T>.()->Unit): RequestBuilder<T> {
    var requestItem: RequestItem? =null
    if(null!=action){
        requestItem = Configuration[action]
        HttpLog.log{
            if(null==requestItem){
                append("$action 获取网络配置模块失败!\n")
            } else {
                append("获取网络配置模块:$action-----------------------\n")
                append("\turl:${requestItem?.url}\n")
                append("\tinfo:${requestItem?.info}\n")
                append("\tmethod:${requestItem?.method}\n")
                append("\tparams:[${requestItem?.params?.joinToString { it }}]\n")
                append("--------------------------------------------\n")
            }
        }
    }
    val requestBuilder = RequestBuilder<T>().apply(request)
    val config=requestBuilder.config
    config.action=action
    if(null!=requestItem){
        //请求网络
        config.info=requestItem.info
        config.url=requestItem.url
        config.method=requestItem.method
        config.info=requestItem.info
        //设置插值
        requestBuilder.pathValue?.let { config.pathValue.addAll(it) }
        //设置entity
        requestBuilder.entity?.let { requestBuilder.config.entity= JSON_MEDIA_TYPE.to(it) }
        //合并模板参数与值
        if(requestItem.params.size==requestBuilder.params.size){
            println(requestBuilder.config.params)
            requestItem.params.zip(requestBuilder.params).
                    filter { null!=it.second }.
                    forEach { (first, second) -> config.params[first]=second  }
        }
        //附加参数,并过滤掉值为空的参数
        requestBuilder.ext?.let { config.params.putAll(it.filterValues { null!=it }) }
    }
    //添加配置header
    val headers=requestBuilder.headers
    if(null!=headers){
        config.header.putAll(headers)
    }
    HttpLog.log{
        append("请求信息:${String.format(config.url,config.pathValue)}-----------------\n")
        append("\turl:${config.url}\n")
        append("\tinfo:${config.info}\n")
        append("\tmethod:${config.method}\n")
        append("\tpathValue:${config.pathValue}\n")
        append("\tentity:${config.entity}\n")
        append("\tparams:${config.params}\n")
        append("\theader:${config.header}\n")
        append("--------------------------------------------------------\n")
    }
    return requestBuilder
}

/**
 * activity
 */
fun<T> Activity.request(tag:String?=null,action:String?=null, request: RequestBuilder<T>.()->Unit){
    val item = getRequestItem(action, request)
    val identityHashCode=System.identityHashCode(this)
    interceptRequest(applicationContext,item.config,item.handler){
        RequestClient.request(getAnyTag(tag,identityHashCode),item){
            val className=this::class.java.simpleName
            val condition=!item.contextDetection or
                    if(Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN_MR1) null!=window.decorView.windowToken else !isDestroyed||null!=window.decorView.windowToken
            HttpLog.log{ append("Activity:$className Tag:$tag 上下文检测:$condition ") }
            condition
        }
    }
}
fun<T> Activity.request(action:String?=null, request: RequestBuilder<T>.()->Unit)=request(null,action,request)

/**
 * activity
 */
fun<T> Activity.syncRequest(tag:String?=null,action:String?=null, request: RequestBuilder<T>.()->Unit){
    val item = getRequestItem(action, request)
    val identityHashCode=System.identityHashCode(this)
    interceptRequest(applicationContext,item.config,item.handler){
        RequestClient.syncRequest(getAnyTag(tag,identityHashCode),item){
            val className=this::class.java.simpleName
            val condition=!item.contextDetection or
                    if(Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN_MR1) null!=window.decorView.windowToken else !isDestroyed||null!=window.decorView.windowToken
            HttpLog.log{ append("Activity:$className Tag:$tag 上下文检测:$condition ") }
            condition
        }
    }
}
fun<T> Activity.syncRequest(action:String?=null, request: RequestBuilder<T>.()->Unit)=syncRequest(null,action,request)

fun Activity.cancelRequest(tag:String?=null)=RequestClient.cancel(tag,this)

/**
 * v4 fragment
 */
fun<T> Fragment.request(tag:String?=null,action:String?=null, request: RequestBuilder<T>.()->Unit){
    val item = getRequestItem(action, request)
    val identityHashCode=System.identityHashCode(this)
    interceptRequest(context,item.config,item.handler){
        RequestClient.request(getAnyTag(tag,identityHashCode),item){
            val className=this::class.java.simpleName
            val condition=!item.contextDetection ||!isDetached&&null!=view?.windowToken
            HttpLog.log{ append("Fragment:$className Tag:$tag 上下文检测:$condition") }
            condition
        }
    }
}

/**
 * v4 fragment
 */
fun<T> Fragment.syncRequest(tag:String?=null,action:String?=null, request: RequestBuilder<T>.()->Unit){
    val item = getRequestItem(action, request)
    val identityHashCode=System.identityHashCode(this)
    interceptRequest(context,item.config,item.handler){
        RequestClient.syncRequest(getAnyTag(tag,identityHashCode),item){
            val className=this::class.java.simpleName
            val condition=!item.contextDetection ||!isDetached&&null!=view?.windowToken
            HttpLog.log{ append("Fragment:$className Tag:$tag 上下文检测:$condition") }
            condition
        }
    }
}

fun<T> Fragment.request(action:String?=null, request: RequestBuilder<T>.()->Unit):Unit=request(null,action,request)

fun<T> Fragment.syncRequest(action:String?=null, request: RequestBuilder<T>.()->Unit):Unit=syncRequest(null,action,request)

fun Fragment.cancelRequest(tag:String?=null)=RequestClient.cancel(tag,this)

/**
 * v4 dialogFragment
 */
fun<T> DialogFragment.request(tag:String?=null,action:String?=null, request: RequestBuilder<T>.()->Unit){
    val item = getRequestItem(action, request)
    val identityHashCode=System.identityHashCode(this)
    interceptRequest(context,item.config,item.handler){
        RequestClient.request(getAnyTag(tag,identityHashCode), item){
            val className=this::class.java.simpleName
            val condition=!item.contextDetection ||!isDetached&&null!=view?.windowToken
            HttpLog.log{ append("DialogFragment:$className Tag:$tag 上下文检测:$condition") }
            condition
        }
    }
}

/**
 * any item
 */
inline fun<T> Any.request(context:Context,tag:String?=null, action:String?=null,noinline request: RequestBuilder<T>.()->Unit){
    val item = getRequestItem(action, request)
    val identityHashCode=System.identityHashCode(this)
    interceptRequest(context,item.config,item.handler){
        RequestClient.request(getAnyTag(tag,identityHashCode), item){ true }
    }
}

inline fun<reified I> I.cancelRequest(tag:String?=null)=RequestClient.cancel(tag,this as Any)

/**
 * 请求前置处理
 */
fun<T> interceptRequest(context:Context?,item:RequestConfig,handler:RequestHandler<T>, closure:()->Unit){
    val enableNetwork=enableNetWork(context)
    val interceptor = requestConfig.networkInterceptor?.invoke(item)
    if(enableNetwork&&(null==interceptor||!interceptor)){
        closure.invoke()
    } else if(!enableNetwork){
        //无网络
        handler.noNetWork.invoke()
    }
}

fun getAnyTag(tag:String?=null,any:Any):String=if(null!=tag) "$any$tag" else "$any"

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
@SuppressLint("MissingPermission")
fun isWifi(context:Context?): Boolean {
    val context=context?:return false
    var result=false
    val systemService = context.getSystemService(Context.CONNECTIVITY_SERVICE)
    if(systemService is ConnectivityManager){
        result=systemService.activeNetworkInfo?.type==ConnectivityManager.TYPE_WIFI
    }
    return result
}

@SuppressLint("MissingPermission")
fun isMobile(context:Context?): Boolean {
    val context=context?:return false
    var result=false
    val systemService = context.getSystemService(Context.CONNECTIVITY_SERVICE)
    if(systemService is ConnectivityManager){
        result=systemService.activeNetworkInfo?.type==ConnectivityManager.TYPE_MOBILE
    }
    return result
}