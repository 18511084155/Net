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
    var params= mutableListOf<Pair<String,String>>()
    var partItems= mutableListOf<Pair<String, File>>()
    var header= mutableListOf<Pair<String,String>>()
}