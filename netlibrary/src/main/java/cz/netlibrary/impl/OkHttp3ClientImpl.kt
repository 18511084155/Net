package cz.netlibrary.impl

import android.text.TextUtils
import cz.netlibrary.callback.RequestCallback
import cz.netlibrary.exception.HttpException
import cz.netlibrary.log.HttpLog
import cz.netlibrary.model.RequestConfig
import cz.netlibrary.model.RequestMethod
import okhttp3.*
import java.io.File
import java.io.IOException

/**
 * Created by cz on 2017/6/7.
 * okhttp3请求操作实例对象
 */
class OkHttp3ClientImpl : BaseRequestClient<Response>() {
    val callItems= mutableMapOf<String,MutableList<Call>>()
    
    override fun syncCall(tag: String, item: RequestConfig, callback: RequestCallback<Response>?): Response? {
        var call:Call?
        var response:Response?=null
        val st = System.currentTimeMillis()
        val errorMessage = requestConfig.errorMessage
        try {
            val request = getRequest(tag, item)
            HttpLog.log { append("发起请求:${request.url()}\n") }
            call = httpClient.newCall(request)
            response = call.execute()
            handleResponse(tag, response, request.url().toString(), st, callback)
        } catch (e: Exception) {
            //request failed
            if(null!=response){
                HttpLog.log { append("请求失败:${response?.request()?.url()}\n耗时:${System.currentTimeMillis()-st} 移除Tag:$tag\n") }
                callback?.onFailed(HttpException(response.code(),getResponseResult(response)))
            } else {
                HttpLog.log { append("请求操作异常:${e.message}\n") }
                callback?.onFailed(HttpException(-1,errorMessage?:e.message))
            }
        } finally {
            callItems.remove(tag)
        }
        return response
    }

    override fun call(tag: String, item: RequestConfig,callback:RequestCallback<Response>?) {
        var call:Call?
        val st = System.currentTimeMillis()
        val errorMessage = requestConfig.errorMessage
        try {
            val request = getRequest(tag, item)
            HttpLog.log { append("发起请求:${request.url()}\n") }
            call = httpClient.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cancel(tag)
                    HttpLog.log { append("请求失败:${request.url()}\n耗时:${System.currentTimeMillis()-st} 移除Tag:$tag\n") }
                    val httpException=HttpException(-1,errorMessage?:e.message)
                    callback?.onFailed(httpException)
                    requestConfig.requestCallback?.invoke(null,-1,httpException)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    cancel(tag)
                    handleResponse(tag, response, request.url().toString(), st, callback)
                }
            })
        } catch (e: Exception) {
            //request failed
            call=null
            HttpLog.log { append("请求操作异常:${e.message}\n") }
            callback?.onFailed(HttpException(-1,errorMessage?:e.message))
        }
        call?.let {
            if(null==callItems[tag]){
                callItems[tag]= mutableListOf()
            }
            callItems[tag]?.add(it)
            HttpLog.log {
                append("请求添加Tag:$tag\n")
                append("当前网络请求数:${callItems.map { it.value.size }.fold(0){total, next -> total + next}}\n")
            }
        }
    }

    private fun handleResponse(tag: String, response: Response, url:String, st: Long, callback: RequestCallback<Response>?) {
        var result: String = getResponseResult(response)
        val code = response.code()
        HttpLog.log { append("请求成功:$url\n请求返回值:$code\n耗时:${System.currentTimeMillis() - st} 移除:Tag:$tag\n") }
        if (200 == code) {
            callback?.onSuccess(response, response.code(), result, (System.currentTimeMillis() - st))
            requestConfig.requestCallback?.invoke(result, response.code(), null)
        } else {
            HttpLog.log { append("请求异常:$code\n结果$result\n") }
            val requestErrorCallback = requestConfig.requestErrorCallback
            if (null == requestErrorCallback) {
                callback?.onFailed(HttpException(code, result))
            } else {
                callback?.onFailed(requestErrorCallback.invoke(code, result))
            }
        }
    }

    /**
     * 获得返回结果值
     */
    private fun getResponseResult(response: Response): String {
        var result: String
        if (null != response.cacheResponse()) {
            result = response.cacheResponse().toString()
        } else {
            result = response.body().string().toString()
        }
        return result
    }

    override fun cancel(tag: String) {
        try{
            val items=callItems.remove(tag)
            items?.let { it.forEach { if(!it.isCanceled)it.cancel() } }
        } catch (e:Exception){
            HttpLog.log { append("取消任务:$tag 发生异常:\n${e.message}\n") }
        }
    }

    private fun getRequest(tag: String, item: RequestConfig): Request {
        val requestUrl = item.getRequestUrl()
        var url:StringBuilder
        if(!item.pathValue.isEmpty()){
            url=StringBuilder(String.format(requestUrl, *item.pathValue.toTypedArray()))
        } else {
            url= StringBuilder(requestUrl)
        }
        HttpLog.log { append("请求url:$url \n") }
        //add extras param
        requestConfig.requestExtrasCallback?.invoke()?.let {  item.params.putAll(it) }
        var request=when(item.method){
            RequestMethod.post, RequestMethod.put-> getMultipartRequest(tag,url,item)
            RequestMethod.get->getGetRequest(tag,url,item)
        }
        //add cookie
        val cookie = item.cookies.map { it.key.to(it.value) }.joinToString("&") { "${it.first}=${it.second}" }
        HttpLog.log { append("cookie:$cookie\n") }
        val newBuilder = request.newBuilder()
        newBuilder.header("Cookie", cookie)
        request = newBuilder.build()
        return request
    }

    private fun getMultipartRequest(tag: String?, url:StringBuilder, item: RequestConfig):Request{
        var requestBody: RequestBody?
        val entity=item.entity
        if (null != entity&&!TextUtils.isEmpty(entity.second)) {
            requestBody = RequestBody.create(MediaType.parse(entity.first), entity.second)
//            HttpLog.d("Request entity:" + requestUrl + "\nmediaType:" + item.entity.first + "\nJson:\n" + item.entity)
        } else {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            item.params.forEach { (key,value)->
                value?.let {
                    if(it !is File){
                        builder.addFormDataPart(key,value.toString())
                    } else {
                        requestBody = RequestBody.create(STREAM, key)
                        builder.addFormDataPart(key, it.name, requestBody)
                    }
                }
            }
            requestBody = builder.build()
        }
        val requestBuilder = Request.Builder().url(url.toString())
        when(item.method){
            RequestMethod.post->requestBuilder.post(requestBody)
            RequestMethod.put->requestBuilder.put(requestBody)
        }
        initRequestBuild(tag, item, requestBuilder)
        return requestBuilder.build()
    }

    /**
     * 获取一个get请求对象
     */
    private fun getGetRequest(tag: String?,url:StringBuilder,item: RequestConfig):Request{
        url.append(item.params.map { it.key.to(it.value) }.joinToString("&") { "${it.first}=${it.second}"})
        val requestBuilder = Request.Builder().url(url.toString())
        initRequestBuild(tag, item, requestBuilder)
        return requestBuilder.build()
    }


    /**
     * 初始化requestBuilder对象,主要用于添加header 以及全局header
     */
    private fun initRequestBuild(tag: String?, item: RequestConfig, requestBuilder: Request.Builder) {
        val headerBuilder = StringBuilder()
        //add custom header items
        item.header.forEach {
            headerBuilder.append(it.key + "=" + it.value + ";")
            requestBuilder.addHeader(it.key, it.value)
        }
        requestConfig.requestHeaderCallback?.invoke()?.let {
            it.forEach {
                headerBuilder.append(it.key + "=" + it.value + ";")
                requestBuilder.addHeader(it.key, it.value)
            }
        }
        tag?.let { requestBuilder.tag(it) }
    }


}