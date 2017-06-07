package cz.netlibrary.exception

/**
 * Created by cz on 2017/6/7.
 */
class HttpException(val code:Int,message:String?):Exception(message) {
    var result: String? = null
    var params= mutableMapOf<String,String>()
    var headers= mutableMapOf<String,String>()
}