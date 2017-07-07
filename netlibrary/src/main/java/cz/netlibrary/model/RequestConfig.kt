package cz.netlibrary.model

/**
 * Created by cz on 2017/6/7.
 */
class RequestConfig{
    var action:String?=null
    var method = RequestMethod.get
    var url:String=String()
    var info:String?=null
    var pathValue = mutableListOf<String>()
    var entity:Pair<String,String>?=null
    var cookies= mutableMapOf<String,String>()
    var params= mutableMapOf<String,Any?>()
    var header= mutableMapOf<String,String>()
}