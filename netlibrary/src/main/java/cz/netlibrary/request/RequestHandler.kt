package cz.netlibrary.request

import cz.netlibrary.exception.HttpException

/**
 * Created by cz on 2017/6/7.
 */
class RequestHandler<T>{
    var map: ((String) -> T)? = null
    var success: (T) -> Unit={}
    var failed: (HttpException) -> Unit={}
    var noNetWork:()->Unit={}
}