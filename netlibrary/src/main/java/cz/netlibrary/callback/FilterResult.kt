package cz.netlibrary.callback

/**
 * Created by cz on 2017/12/29.
 */
interface FilterResult<R>{
    fun call(result:String):R
}