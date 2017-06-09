package cz.netlibrary.model

import java.io.File

/**
 * Created by cz on 2017/6/7.
 */
class RequestConfig{
    var method = RequestMethod.get
    var url:String=String()
    var info:String?=null
    var pathValues= mutableListOf<String>()
    var templateName= mutableListOf<String>()
    var templateValue= mutableListOf<Any>()
    var entity:Pair<String,String>?=null
    var cookies= mutableListOf<Pair<String,String>>()
    var params= mutableListOf<Pair<String,String>>()
    var header= mutableListOf<Pair<String,String>>()
    var partItems= mutableListOf<Pair<String, File>>()
}