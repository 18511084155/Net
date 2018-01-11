package cz.netlibrary.request

import cz.netlibrary.callback.FilterResult
import cz.netlibrary.callback.RequestFailCallback
import cz.netlibrary.callback.RequestSuccessCallback
import cz.netlibrary.exception.HttpException

/**
 * Created by cz on 2017/6/7.
 */
class RequestHandler<T>{
    var map: FilterResult<T>? = null
    var success: ((T) -> Unit)?=null
    var successCallback: RequestSuccessCallback<T>?=null
    var failedCallback: RequestFailCallback?=null
    var failed: ((HttpException) -> Unit)?=null
    var noNetWork:()->Unit={}
}