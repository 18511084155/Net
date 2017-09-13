package net.sample.ui

import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cz.loglibrary.LogConfig
import com.cz.loglibrary.impl.JsonPrinter
import cz.netlibrary.request
import kotlinx.android.synthetic.main.fragment_template.*
import net.sample.ProgressDialogLifeCycle
import net.sample.R
import net.sample.prefs.NetWorkPrefs
import net.sample.utils.ConfigManager
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * Created by cz on 2017/6/23.
 */
class TemplateFragment:Fragment(){
    val APP_KEY ="ab54e19d080"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_template,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requestButton.onClick { templateRequest() }
    }

    fun templateRequest(){
        val formatter= JsonPrinter()
        formatter.setLogConfig(LogConfig.get())
        ConfigManager.request(context)
//        request<String>(NetWorkPrefs.RAIL_WAY_TRAIN) {
//            params= arrayOf(APP_KEY,"G2")
//            map{
//                //延持时间,检测上下文是否存在
//                SystemClock.sleep(1*1000)
//                it
//            }
//            lifeCycleItem(ProgressDialogLifeCycle(context,"加载中")){true}
//            success {
//                contentView.text=formatter.format(it).reduce { acc, s -> acc+s }
//            }
//            failed { (code,message)->
//                contentView.text=message
//            }
//        }
    }
}