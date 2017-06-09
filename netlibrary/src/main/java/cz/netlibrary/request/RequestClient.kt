package cz.netlibrary.request

import android.os.Handler
import android.os.Looper
import cz.netlibrary.callback.RequestCallback
import cz.netlibrary.exception.HttpException
import cz.netlibrary.impl.OkHttp3ClientImpl
import cz.netlibrary.model.RequestConfig
import okhttp3.Response

/**
 * Created by cz on 2017/6/7.
 * 请求执行客户端
 */
object RequestClient{
    val client= OkHttp3ClientImpl()

    fun<T> request(tag:String,item:RequestConfig,handler: RequestHandler<T>,contextCondition:()->Boolean){
        handler.lifeCycle?.call(RequestLifeCycle.START)
        client.call(tag,item,object:RequestCallback<Response> {
            override fun onSuccess(response: Response, code: Int, result: String, time: Long) {
                if(contextCondition.invoke()){
                    handler.lifeCycle?.call(RequestLifeCycle.BEFORE_CALL)
                    val item = handler.map.invoke(result)
                    //回调处理结果
                    if(contextCondition.invoke()){
                        if(handler.mainThread&&ContextHelper.mainThread==Thread.currentThread()){
                            item?.let { handler.success.invoke(it) }
                            handler.lifeCycle?.call(RequestLifeCycle.AFTER_CALL)
                        } else {
                            ContextHelper.handler.post {
                                item?.let { handler.success.invoke(it) }
                                handler.lifeCycle?.call(RequestLifeCycle.AFTER_CALL)
                            }
                        }
                    }
                }
            }

            override fun onFailed(exception: HttpException) {
                if(contextCondition.invoke()){
                    //回调异常结果
                    handler.lifeCycle?.call(RequestLifeCycle.BEFORE_FAILED)
                    if(handler.mainThread&&ContextHelper.mainThread==Thread.currentThread()){
                        item?.let { handler.failed.invoke(exception) }
                        handler.lifeCycle?.call(RequestLifeCycle.AFTER_FAILED)
                    } else {
                        ContextHelper.handler.post {
                            item?.let { handler.failed.invoke(exception) }
                            handler.lifeCycle?.call(RequestLifeCycle.AFTER_FAILED)
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