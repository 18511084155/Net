package cz.netlibrary.request

import cz.netlibrary.callback.LifeCycleCallback
import cz.netlibrary.exception.HttpException
import cz.netlibrary.model.RequestConfig

/**
 * Created by cz on 2017/6/7.
 */
class RequestBuilder<T>{
    val config=RequestConfig()
    val handler=RequestHandler<T>()
    //线程调度
    var mainThread=true
    //检测上下文
    var contextCondition=true
    //请求生命周期
    var lifeCycle: LifeCycleCallback?=null

    //配置一个get请求信息
    inline fun get(closure: GetRequest.() -> Unit){
        val request = GetRequest().apply(closure)
        config.info=request.info
        config.url=request.url
        config.method=request.method
        config.params.addAll(request.params)
        config.header.addAll(request.header)
    }

    //配置一个post请求信息
    inline fun post(closure: PostRequest.() -> Unit){
        val request = PostRequest().apply(closure)
        config.info=request.info
        config.url=request.url
        config.method=request.method
        config.entity=request.entity
        config.params.addAll(request.params)
        config.header.addAll(request.header)
        config.partItems.addAll(request.partItems)
    }

    //过滤信息
    fun map(map: (String) -> T?){
        this.handler.map=map
    }

    //完成回调
    fun success(success: (T) -> Unit){
        this.handler.success=success
    }

    //请求失败回调
    fun failed(failed: (HttpException) -> Unit){
        this.handler.failed=failed
    }
    //无网络
    fun noNetWork(default:Boolean=false,closure:()->Unit){
        this.handler.noNetWork=closure
    }
}