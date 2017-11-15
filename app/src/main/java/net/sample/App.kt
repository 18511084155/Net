package net.sample

import android.app.Application
import com.cz.loglibrary.JLog
import com.cz.loglibrary.LogConfig
import cz.netlibrary.init
import cz.netlibrary.request
import cz.netlibrary.request.RequestClient

/**
 * Created by cz on 2017/6/7.
 */
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        //初始化网络配置
        init {
            url="http://apicloud.mob.com/"
            writeTimeout=10*1000
            readTimeout=10*1000
            httpLog=true

        }
        JLog.setLogConfig(LogConfig.get().setLogLevel(JLog.ALL))
    }
}