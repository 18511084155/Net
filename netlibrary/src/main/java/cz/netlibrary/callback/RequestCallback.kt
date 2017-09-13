package cz.netlibrary.callback

import cz.netlibrary.exception.HttpException

/**
 * Created by cz on 2017/6/7.
 */
interface RequestCallback<in T> {
    fun onSuccess(response:T, code:Int, result:String, time:Long)
    fun onFailed(code:Int,message:String?,result:String?)
}