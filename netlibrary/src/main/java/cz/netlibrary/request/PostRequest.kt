package cz.netlibrary.request

import cz.netlibrary.model.RequestMethod
import java.io.File

/**
 * Created by cz on 2017/6/7.
 */
class PostRequest{
    var method = RequestMethod.post
    var info:String?=null
    var url:String=String()
    var entity:Pair<String,String>?=null
    var pathValue:Array<String>?=null
    var params:Map<String,Any?>?=null
    var header:Map<String,String>?=null
}