package cz.netlibrary.request

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import cz.netlibrary.*
import cz.netlibrary.callback.RequestCallback
import cz.netlibrary.exception.HttpException
import cz.netlibrary.impl.BaseRequestClient
import cz.netlibrary.impl.OkHttp3ClientImpl
import cz.netlibrary.log.HttpLog
import okhttp3.Response

/**
 * Created by cz on 2017/6/7.
 * 请求执行客户端
 */
object RequestClient{
    val client= OkHttp3ClientImpl()

    fun<T> request(tag:String, requestItem: RequestBuilder<T>, contextCondition:()->Boolean){
        val config= requestItem.config
        val mainThread= requestItem.mainThread
        val handler= requestItem.handler
        val abortOnError = BaseRequestClient.requestConfig.abortOnError
        val errorMessage = BaseRequestClient.requestConfig.errorMessage
        requestItem.lifeCycle?.invoke(RequestLifeCycle.START)
        requestItem.lifeCycleItem?.call(RequestLifeCycle.START)
        client.call(tag,config,object:RequestCallback<Response> {
            override fun onSuccess(response: Response, code: Int, result: String, time: Long) {
                if(!contextCondition.invoke()){
                    lifeCycleCall(RequestLifeCycle.CANCEL)
                } else {
                    lifeCycleCall(RequestLifeCycle.BEFORE_CALL)
                    executeOnError {
                        HttpLog.log { append("请求成功:${response.request().url()}") }
                        val item = handler.map?.invoke(result)?:null
                        if(null==item){
                            executeOnThread {
                                HttpLog.log { append("数据处理失败$result -> map:${handler.map}!\n") }
                                handler.failed.invoke(HttpException(-1,errorMessage?:"数据处理失败!"))
                            }
                        } else {
                            HttpLog.log { append("数据处理:${item?.toString()}\n") }
                            //回调处理结果
                            if (!contextCondition.invoke()) {
                                lifeCycleCall(RequestLifeCycle.CANCEL)
                            } else {
                                HttpLog.log { append("回调线程:$mainThread\n") }
                                executeOnThread { item?.let { handler.success.invoke(it) } }
                            }
                        }
                    }?.apply {
                        executeOnThread {
                            HttpLog.log { append("请求成功但执行异常:$message\n") }
                            handler.failed.invoke(HttpException(-1,errorMessage?:message))
                        }
                    }
                    lifeCycleCall(RequestLifeCycle.AFTER_CALL)
                    lifeCycleCall(RequestLifeCycle.FINISH)
                }
            }

            override fun onFailed(exception: HttpException) {
                if(!contextCondition.invoke()){
                    lifeCycleCall(RequestLifeCycle.CANCEL)
                } else {
                    //回调异常结果
                    lifeCycleCall(RequestLifeCycle.BEFORE_FAILED)
                    HttpLog.log { append("异常回调线程:$mainThread\n") }
                    HttpLog.log {
                        append("\tcode:${exception.code}\n")
                        append("\tmessage:${exception.message}\n")
                        append("-----------------------------stackTrace-----------------------------\n")
                        Thread.currentThread().stackTrace.forEach { append(it.toString()+"\n") }
                    }
                    executeOnThread { handler.failed.invoke(exception) }
                    lifeCycleCall(RequestLifeCycle.AFTER_CALL)
                    lifeCycleCall(RequestLifeCycle.FINISH)
                }
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
                    executeOnError { closure.invoke() }
                } else if(mainThread){
                    ContextHelper.handler.post { executeOnError { closure.invoke() } }
                }
            }

            /**
             * 请求生命周期回调,确保在子线程回调
             */
            fun lifeCycleCall(lifeCycle: RequestLifeCycle){
                ContextHelper.handler.post {
                    requestItem.lifeCycle?.invoke(lifeCycle)
                    requestItem.lifeCycleItem?.call(lifeCycle)
                }
            }
        })
    }



    fun cancel(tag:String?=null,any:Any){
        client.cancel(if(null!=tag) any.javaClass.simpleName+tag else any.javaClass.simpleName)
    }


    private object ContextHelper {
        val handler = Handler(Looper.getMainLooper())
        val mainThread: Thread = Looper.getMainLooper().thread
    }
}