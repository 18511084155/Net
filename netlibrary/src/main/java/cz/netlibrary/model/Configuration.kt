package cz.netlibrary.model

/**
 * Created by cz on 2017/6/7.
 */
object Configuration {
    val requestItems= mutableListOf<RequestItem>()

    fun init(vararg items:RequestItem.()->Unit){
        items.forEach { requestItems.add(RequestItem().apply(it)) }
    }

    //重载运算符 get 可在使用RequestManager["action"]获取请求条目
    operator fun get(action:String): RequestItem? = requestItems.find { it.action==action }
}