package cz.netlibrary.log

import android.util.Log

/**
 * Created by Administrator on 2017/6/9.
 */
object HttpLog{
    val TAG="HttpLog"
    var httpLog=false

    fun log(action:StringBuilder.()->Unit){
        if(httpLog){ Log.i(TAG,StringBuilder().apply(action).toString()) }
    }

}