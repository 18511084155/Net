package cz.netlibrary.request

import cz.netlibrary.callback.LifeCycleCallback
import cz.netlibrary.exception.HttpException

/**
 * Created by cz on 2017/6/7.
 */
class RequestHandler<T>{
    var mainThread=true
    var contextCondition=true
    var map: (String) -> T? = { null }
    var lifeCycle: LifeCycleCallback?=null
    var success: (T) -> Unit={}
    var failed: (HttpException) -> Unit={}
    var noNetWork:()->Unit={}
}