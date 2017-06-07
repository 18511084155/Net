package cz.netlibrary.callback

import cz.netlibrary.exception.HttpException

/**
 * Created by cz on 2017/6/7.
 */
interface RequestCallback<in T> {
    fun onSuccess(t:T,code:Int,result:String,time:Long)
    fun onFailed(exception:HttpException)
}