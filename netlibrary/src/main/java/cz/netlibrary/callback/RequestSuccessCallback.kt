package cz.netlibrary.callback

/**
 * Created by cz on 2017/7/17.
 */
interface RequestSuccessCallback<in T>{
    fun onSuccess(item:T)
}