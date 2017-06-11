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
    var contextDetection =true
    //请求生命周期
    var lifeCycle: LifeCycleCallback?=null
    //模板请求参数
    var params= arrayOf<Any>()
    //模板请求entity
    var entity:String?=null
    //配置一个get请求信息
    inline fun get(closure: GetRequest.() -> Unit){
        val request = GetRequest().apply(closure)
        config.info=request.info
        config.url=request.url
        config.method=request.method
        request.params?.let { config.params.putAll(it) }
        request.header?.let { config.header.putAll(it) }
        request.pathValue?.let { config.pathValue.addAll(it) }
    }

    //配置一个post请求信息
    inline fun post(closure: PostRequest.() -> Unit){
        val request = PostRequest().apply(closure)
        config.info=request.info
        config.url=request.url
        config.method=request.method
        config.entity=request.entity
        request.params?.let { config.params.putAll(it) }
        request.header?.let { config.header.putAll(it) }
        request.partItems?.let { config.partItems.putAll(it) }
        request.pathValue?.let { config.pathValue.addAll(it) }
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
    fun noNetWork(closure:()->Unit){
        this.handler.noNetWork=closure
    }
}