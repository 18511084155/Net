package cz.netlibrary.request

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import cz.netlibrary.OPERATION_FAILED
import cz.netlibrary.callback.RequestCallback
import cz.netlibrary.exception.HttpException
import cz.netlibrary.impl.OkHttp3ClientImpl
import cz.netlibrary.log.HttpLog
import cz.netlibrary.requestConfig
import okhttp3.Response

/**
 * Created by cz on 2017/6/7.
 * 请求执行客户端
 */
object RequestClient{
    val client= OkHttp3ClientImpl()

    /**
     * 获得请求对象
     */
    fun getHttpClient()= client.getHttpClient()

    fun<T> syncRequest(tag:String, requestItem: RequestBuilder<T>, contextCondition:()->Boolean){
        client.syncCall(tag,requestItem.config,HttpRequestCallback(requestItem,contextCondition))
    }

    fun<T> request(tag:String, requestItem: RequestBuilder<T>, contextCondition:()->Boolean){
        client.call(tag,requestItem.config,HttpRequestCallback(requestItem,contextCondition))
    }

    fun cancel(tag:String?=null,any:Any){
        client.cancel(if(null!=tag) any.javaClass.simpleName+tag else any.javaClass.simpleName)
    }

    private object ContextHelper {
        val handler = Handler(Looper.getMainLooper())
        val mainThread: Thread = Looper.getMainLooper().thread
    }


    class HttpRequestCallback<T>(private val requestItem: RequestBuilder<T>, private val contextCondition:()->Boolean):RequestCallback<Response>{
        private val abortOnError = requestConfig.abortOnError
        private val errorMessage = requestConfig.errorMessage
        private val requestErrorCallback=requestConfig.requestErrorCallback
        private val mainThread= requestItem.mainThread
        private val handler= requestItem.handler
        init {
            executeOnThread{ lifeCycleCall(RequestLifeCycle.START) }
        }
        override fun onSuccess(response: Response, code: Int, result: String, time: Long) {
            if(!contextCondition.invoke()){
                lifeCycleCall(RequestLifeCycle.CANCEL)
            } else {
                lifeCycleCall(RequestLifeCycle.BEFORE_CALL)
                executeOnError {
                    HttpLog.log { append("请求成功:${response.request().url()}") }
                    //此处requestSuccessCallback可将结果再做二次转换比如:{message:"" code:"",item:{}} 提取出item,再交给map转换
                    var convertValue=result
                    if(TextUtils.isEmpty(convertValue)){
                        HttpLog.log { append("空数据$result\n") }
                        executeOnThread { callFailed(OPERATION_FAILED,errorMessage?:"请求没有结果!") }
                    } else {
                        var item: T?=null
                        try{
                            item = handler.map?.call(convertValue)
                        } catch (e:Exception){
                            executeOnThread {
                                HttpLog.log { append("数据处理失败$result -> map:${handler.map}!\n") }
                                if(e is HttpException){
                                    callFailed(e.code,e.message?:"数据处理失败!")
                                } else {
                                    callFailed(OPERATION_FAILED,e.message?:"数据处理失败!")
                                }
                            }
                            return@executeOnError
                        }
                        if(null==item){
                            executeOnThread {
                                HttpLog.log { append("数据处理失败$result -> map:${handler.map}!\n") }
                                callFailed(OPERATION_FAILED,errorMessage?:"数据处理失败!")
                            }
                        } else {
                            HttpLog.log { append("数据处理:${item?.toString()}\n") }
                            //回调处理结果
                            if (!contextCondition.invoke()) {
                                lifeCycleCall(RequestLifeCycle.CANCEL)
                            } else {
                                HttpLog.log { append("回调线程:$mainThread\n") }
                                executeOnThread {
                                    item?.let {
                                        handler.success?.invoke(it)
                                        handler.successCallback?.onSuccess(it)
                                    }
                                }
                            }
                        }
                    }
                }?.apply {
                    executeOnThread {
                        HttpLog.log { append("请求成功但执行异常:$message\n") }
                        callFailed(OPERATION_FAILED,errorMessage?:message)
                    }
                }
                lifeCycleCall(RequestLifeCycle.AFTER_CALL)
                lifeCycleCall(RequestLifeCycle.FINISH)
            }
        }

        override fun onFailed(code:Int,message:String?,result:String?) {
            if(!contextCondition.invoke()){
                lifeCycleCall(RequestLifeCycle.CANCEL)
            } else {
                //回调异常结果
                lifeCycleCall(RequestLifeCycle.BEFORE_FAILED)
                HttpLog.log { append("异常回调线程:$mainThread\n") }
                HttpLog.log {
                    append("\tcode:$code\n")
                    append("\tresult:$result\n")
                    append("-----------------------------stackTrace-----------------------------\n")
                    Thread.currentThread().stackTrace.forEach { append(it.toString()+"\n") }
                }
                executeOnThread { callFailed(code,message,result) }
                lifeCycleCall(RequestLifeCycle.AFTER_FAILED)
                lifeCycleCall(RequestLifeCycle.FINISH)
            }
        }
        fun callFailed(code:Int,message:String?,result:String?=null){
            var exception:HttpException
            if (null == requestErrorCallback) {
                exception=HttpException(code, message)
            } else {
                exception=requestErrorCallback.invoke(code, message, result)
            }
            handler.failed?.invoke(exception)
            handler.failedCallback?.onFailed(exception)
        }
        /**
         * 执行回调
         */
        fun executeOnError(closure:()->Unit):Exception?{
            var error:Exception?=null
            if(abortOnError){
                closure.invoke()
            } else {
                try{
                    closure.invoke()
                } catch (e:Exception){
                    error=e
                    e.printStackTrace()
                }
            }
            return error
        }

        /**
         * 执行回调,并根据mainThread标记,设定回调线程
         */
        fun executeOnThread(closure:()->Unit){
            if(!mainThread||mainThread&&ContextHelper.mainThread==Thread.currentThread()){
                executeOnError { closure.invoke() }?.apply { HttpLog.log { append("未知的执行异常:$message\n") } }
            } else if(mainThread){
                ContextHelper.handler.post { executeOnError { closure.invoke() }?.apply { HttpLog.log { append("未知的执行异常:$message\n") } } }
            }
        }

        /**
         * 请求生命周期回调,确保在子线程回调
         */
        fun lifeCycleCall(lifeCycle: RequestLifeCycle){
            val condition=requestItem.lifeCycleCondition
            if(null==condition||condition.invoke()){
                ContextHelper.handler.post {
                    requestItem.lifeCycle?.invoke(lifeCycle)
                    requestItem.lifeCycleItem?.call(lifeCycle)
                }
            }
        }
    }
}