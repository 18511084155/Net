package cz.netlibrary.model

import cz.netlibrary.model.RequestMethod.get
import cz.netlibrary.model.RequestMethod.post
import cz.netlibrary.model.RequestMethod.put
/**
 * Created by cz on 2017/6/7.
 */
class RequestItem {
    var action: String? = null// 请求方法
    var method=RequestMethod.get// 请求get/post
    var info: String? = null
    var params = arrayOf<String>()// 请求参数
    var url: String = String()// 请求url前缀
}