package cz.netlibrary.exception

/**
 * Created by cz on 2017/6/7.
 */
open class HttpException(val code:Int,message:String?):Exception(message) {
    operator fun component1(): Int =code
    operator fun component2(): String? =message
}
