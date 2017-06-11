package cz.netlibrary.request

import android.os.Handler
import android.os.Looper
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
        val lifeCycle= requestItem.lifeCycle
        val mainThread= requestItem.mainThread
        val handler= requestItem.handler
        lifeCycle?.call(RequestLifeCycle.START)
        val abortOnError = BaseRequestClient.requestConfig.abortOnError

        client.call(tag,config,object:RequestCallback<Response> {
            override fun onSuccess(response: Response, code: Int, result: String, time: Long) {
                if(contextCondition.invoke()){
                    lifeCycle?.call(RequestLifeCycle.BEFORE_CALL)
                    execute {
                        HttpLog.log { append("请求成功:${response.request().url()}") }
                        val item = handler.map.invoke(result)
                        HttpLog.log { append("数据处理:${item?.toString()}\n") }
                        //回调处理结果
                        if (contextCondition.invoke()) {
                            HttpLog.log { append("回调线程:$mainThread\n") }
                            if (mainThread && ContextHelper.mainThread == Thread.currentThread()) {
                                item?.let { handler.success.invoke(it) }
                                lifeCycle?.call(RequestLifeCycle.AFTER_CALL)
                            } else {
                                ContextHelper.handler.post {
                                    item?.let { handler.success.invoke(it) }
                                    lifeCycle?.call(RequestLifeCycle.AFTER_CALL)
                                }
                            }
                        }
                    }?.apply {
                        HttpLog.log { append("请求成功但执行异常:$message\n") }
                        handler.failed.invoke(HttpException(-1,message))
                    }
                }
            }

            override fun onFailed(exception: HttpException) {
                if(contextCondition.invoke()){
                    //回调异常结果
                    lifeCycle?.call(RequestLifeCycle.BEFORE_FAILED)
                    HttpLog.log { append("异常回调线程:$mainThread\n") }
                    HttpLog.log {
                        append("code:${exception.code}")
                        append("result:${exception.result}")
                        append("headers:${exception.headers}")
                        append("params:${exception.params}")
                        append("message:${exception.message}")
                        append("code:${exception.message}")
                        append("-----------------------------stackTrace-----------------------------\n")
                        Thread.currentThread().stackTrace.forEach { append(it.toString()+"\n") }
                    }
                    if(mainThread&&ContextHelper.mainThread==Thread.currentThread()){
                        execute { handler.failed.invoke(exception) }
                        lifeCycle?.call(RequestLifeCycle.AFTER_FAILED)
                    } else {
                        ContextHelper.handler.post {
                            execute { handler.failed.invoke(exception) }
                            lifeCycle?.call(RequestLifeCycle.AFTER_FAILED)
                        }
                    }
                }
            }
            /**
             * 执行回调
             */
            fun execute(closure:()->Unit):Exception?{
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