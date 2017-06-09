package cz.netlibrary.request

import android.os.Handler
import android.os.Looper
import cz.netlibrary.callback.RequestCallback
import cz.netlibrary.exception.HttpException
import cz.netlibrary.impl.OkHttp3ClientImpl
import okhttp3.Response

/**
 * Created by cz on 2017/6/7.
 * 请求执行客户端
 */
object RequestClient{
    val client= OkHttp3ClientImpl()

    fun<T> request(tag:String, requestBuilder: RequestBuilder<T>, contextCondition:()->Boolean){
        val config=requestBuilder.config
        val lifeCycle=requestBuilder.lifeCycle
        val mainThread=requestBuilder.mainThread
        val handler=requestBuilder.handler
        lifeCycle?.call(RequestLifeCycle.START)
        client.call(tag,config,object:RequestCallback<Response> {
            override fun onSuccess(response: Response, code: Int, result: String, time: Long) {
                if(contextCondition.invoke()){
                    lifeCycle?.call(RequestLifeCycle.BEFORE_CALL)
                    val item = handler.map.invoke(result)
                    //回调处理结果
                    if(contextCondition.invoke()){
                        if(mainThread&&ContextHelper.mainThread==Thread.currentThread()){
                            item?.let { handler.success.invoke(it) }
                            lifeCycle?.call(RequestLifeCycle.AFTER_CALL)
                        } else {
                            ContextHelper.handler.post {
                                item?.let { handler.success.invoke(it) }
                                lifeCycle?.call(RequestLifeCycle.AFTER_CALL)
                            }
                        }
                    }
                }
            }

            override fun onFailed(exception: HttpException) {
                if(contextCondition.invoke()){
                    //回调异常结果
                    lifeCycle?.call(RequestLifeCycle.BEFORE_FAILED)
                    if(mainThread&&ContextHelper.mainThread==Thread.currentThread()){
                        handler.failed.invoke(exception)
                        lifeCycle?.call(RequestLifeCycle.AFTER_FAILED)
                    } else {
                        ContextHelper.handler.post {
                            handler.failed.invoke(exception)
                            lifeCycle?.call(RequestLifeCycle.AFTER_FAILED)
                        }
                    }
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