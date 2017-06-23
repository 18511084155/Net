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
        val lifeCycleItem= requestItem.lifeCycleItem
        val mainThread= requestItem.mainThread
        val handler= requestItem.handler
        lifeCycle?.invoke(RequestLifeCycle.START)
        lifeCycleItem?.call(RequestLifeCycle.START)
        val abortOnError = BaseRequestClient.requestConfig.abortOnError

        client.call(tag,config,object:RequestCallback<Response> {
            override fun onSuccess(response: Response, code: Int, result: String, time: Long) {
                if(!contextCondition.invoke()){
                    lifeCycle?.invoke(RequestLifeCycle.CANCEL)
                    lifeCycleItem?.call(RequestLifeCycle.CANCEL)
                } else {
                    lifeCycle?.invoke(RequestLifeCycle.BEFORE_CALL)
                    lifeCycleItem?.call(RequestLifeCycle.BEFORE_CALL)
                    executeOnError {
                        HttpLog.log { append("请求成功:${response.request().url()}") }
                        val item = handler.map!!.invoke(result)
                        HttpLog.log { append("数据处理:${item?.toString()}\n") }
                        //回调处理结果
                        if (!contextCondition.invoke()) {
                            lifeCycle?.invoke(RequestLifeCycle.CANCEL)
                            lifeCycleItem?.call(RequestLifeCycle.CANCEL)
                        } else {
                            HttpLog.log { append("回调线程:$mainThread\n") }
                            executeOnThread {
                                item?.let { handler.success.invoke(it) }
                                lifeCycle?.invoke(RequestLifeCycle.AFTER_CALL)
                                lifeCycleItem?.call(RequestLifeCycle.AFTER_CALL)
                            }
                        }
                    }?.apply {
                        executeOnThread {
                            HttpLog.log { append("请求成功但执行异常:$message\n") }
                            handler.failed.invoke(HttpException(-1,message))
                        }
                    }
                    ContextHelper.handler.post {
                        lifeCycle?.invoke(RequestLifeCycle.FINISH)
                        lifeCycleItem?.call(RequestLifeCycle.FINISH)
                    }
                }
            }

            override fun onFailed(exception: HttpException) {
                if(!contextCondition.invoke()){
                    lifeCycle?.invoke(RequestLifeCycle.CANCEL)
                    lifeCycleItem?.call(RequestLifeCycle.CANCEL)
                } else {
                    //回调异常结果
                    lifeCycle?.invoke(RequestLifeCycle.BEFORE_FAILED)
                    lifeCycleItem?.call(RequestLifeCycle.BEFORE_FAILED)
                    HttpLog.log { append("异常回调线程:$mainThread\n") }
                    HttpLog.log {
                        append("\tcode:${exception.code}\n")
                        append("\tresult:${exception.result}\n")
                        append("\theaders:${exception.headers}\n")
                        append("\tparams:${exception.params}\n")
                        append("\tmessage:${exception.message}\n")
                        append("-----------------------------stackTrace-----------------------------\n")
                        Thread.currentThread().stackTrace.forEach { append(it.toString()+"\n") }
                    }
                    executeOnThread {
                        handler.failed.invoke(exception)
                        lifeCycle?.invoke(RequestLifeCycle.AFTER_FAILED)
                        lifeCycleItem?.call(RequestLifeCycle.AFTER_FAILED)
                    }
                    ContextHelper.handler.post {
                        lifeCycle?.invoke(RequestLifeCycle.FINISH)
                        lifeCycleItem?.call(RequestLifeCycle.FINISH)
                    }
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