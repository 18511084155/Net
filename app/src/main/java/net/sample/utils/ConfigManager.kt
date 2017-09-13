package net.sample.utils

import android.content.Context
import android.os.SystemClock
import android.widget.Toast
import cz.netlibrary.request
import net.sample.ProgressDialogLifeCycle
import net.sample.prefs.NetWorkPrefs

/**
 * Created by cz on 2017/9/13.
 */
object ConfigManager{
    val APP_KEY ="ab54e19d080"
    fun request(context: Context){
        request<String>(context,action = NetWorkPrefs.WEATHER) {
            params= arrayOf(APP_KEY,"通州","北京")
            map{
                //延持时间,检测上下文是否存在
                SystemClock.sleep(1*1000)
                it
            }
            lifeCycleItem(ProgressDialogLifeCycle(context,"加载中")){true}
            success {
                Toast.makeText(context,"请求成功!",Toast.LENGTH_SHORT).show()
            }
            failed { (code,message)->
                Toast.makeText(context,"请求失败!",Toast.LENGTH_SHORT).show()
            }
        }
    }
}