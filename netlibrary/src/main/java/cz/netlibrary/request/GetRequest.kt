package cz.netlibrary.request

import cz.netlibrary.model.RequestMethod

/**
 * Created by cz on 2017/6/7.
 */
class GetRequest{
    var method=RequestMethod.get
    var info:String?=null
    var url:String=String()
    var pathValue:Array<String>?=null
    var params:Map<String,String>?=null
    var header:Map<String,String>?=null
}