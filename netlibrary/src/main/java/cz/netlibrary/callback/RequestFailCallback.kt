package cz.netlibrary.callback

import cz.netlibrary.exception.HttpException

/**
 * Created by cz on 2017/7/17.
 */
interface RequestFailCallback{
    fun onFailed(e: HttpException)
}