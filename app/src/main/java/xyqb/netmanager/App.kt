package xyqb.netmanager

import android.app.Application
import cz.netlibrary.init

/**
 * Created by cz on 2017/6/7.
 */
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        //初始化网络配置
        init {
            writeTimeout=10*1000
            readTimeout=10*1000
        }
    }
}