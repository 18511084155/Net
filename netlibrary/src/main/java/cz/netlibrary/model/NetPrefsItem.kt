package cz.netlibrary.model

/**
 * Created by cz on 2017/9/19.
 */
open class NetPrefsItem{
    val requestItems= mutableListOf<RequestItem>()

    fun item(closure:RequestItem.()->Unit){
        requestItems.add(RequestItem().apply(closure))
    }
}