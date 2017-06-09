package cz.netlibrary.impl

import android.text.TextUtils
import cz.netlibrary.callback.RequestCallback
import cz.netlibrary.exception.HttpException
import cz.netlibrary.model.RequestConfig
import cz.netlibrary.model.RequestMethod
import okhttp3.*
import java.io.IOException

/**
 * Created by cz on 2017/6/7.
 * okhttp3请求操作实例对象
 */
class OkHttp3ClientImpl : BaseRequestClient<Response>() {
    val JSON = MediaType.parse("application/json; charset=utf-8")
    val TEXT = MediaType.parse("Content-Type application/x-www-form-")
    val STREAM = MediaType.parse("application/octet-stream")

    val callItems= mutableMapOf<String,MutableList<Call>>()
    override fun call(tag: String, item: RequestConfig,callback:RequestCallback<Response>?) {
        var call:Call?
        val st = System.currentTimeMillis()
        try {
            val request = getRequest(tag, item)
            call = httpClient.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callItems.remove(tag)
                    callback?.onFailed(HttpException(-1,e.message))
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    callItems.remove(tag)
                    var result:String
                    if (null != response.cacheResponse()) {
                        result = response.cacheResponse().toString()
                    } else {
                        result = response.body().string().toString()
                    }
                    callback?.onSuccess(response,response.code(),result,(System.currentTimeMillis()-st))
                }
            })
        } catch (e: Exception) {
            //request failed
            call=null
            callback?.onFailed(HttpException(-1,e.message))
        }
        call?.let {
            if(null==callItems[tag]){
                callItems[tag]= mutableListOf()
            }
            callItems[tag]?.add(it)
        }
    }

    override fun cancel(tag: String) {
        try{
            val items=callItems.remove(tag)
            items?.let { it.forEach { if(!it.isCanceled)it.cancel() } }
        } catch (e:Exception){ }
    }

    private fun getRequest(tag: String, item: RequestConfig): Request {
        val requestUrl = item.getRequestUrl()
        val url = StringBuilder(requestUrl)
        if(!item.pathValues.isEmpty()){
            url.append(String.format(requestUrl, item.pathValues))
        }
        //add extras param
        requestConfig.extrasParams?.let { it.invoke()?.forEach { item.params.add(it.first.to(it.second)) } }
        var request=when(item.method){
            RequestMethod.post, RequestMethod.put-> getMultipartRequest(tag,url,item)
            RequestMethod.get->getGetRequest(tag,url,item)
        }
        //add cookie
        val cookie = item.cookies.joinToString("&") { "${it.first}=${it.second}" }
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
            item.params.forEach { builder.addFormDataPart(it.first,it.second) }
            item.partItems.forEach {
                requestBody = RequestBody.create(STREAM, it.second)
                builder.addFormDataPart(it.first, it.second.name, requestBody)
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
        url.append(item.params.joinToString("&") { "${it.first}=${it.second}"})
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
            headerBuilder.append(it.first + "=" + it.second + ";")
            requestBuilder.addHeader(it.first, it.second)
        }
        requestConfig.extrasHeader?.let {
            it.invoke()?.forEach {
                headerBuilder.append(it.first + "=" + it.second + ";")
                requestBuilder.addHeader(it.first, it.second)
            }
        }
        tag?.let { requestBuilder.tag(it) }
    }


}