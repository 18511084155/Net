package net.sample.ui

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.cz.loglibrary.JLog
import com.cz.loglibrary.LogConfig
import com.cz.loglibrary.impl.JsonPrinter
import cz.netlibrary.request
import cz.netlibrary.request.RequestLifeCycle
import kotlinx.android.synthetic.main.activity_context.*
import net.sample.R
import org.jetbrains.anko.sdk25.coroutines.onClick

class ContextActivity : AppCompatActivity() {
    val APP_KEY ="ab54e19d080"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_context)
        title=intent.getStringExtra("title")
        toolBar.title = title
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBar.setNavigationOnClickListener{ finish() }
        requestButton.onClick { getRequest() }
    }

    fun getRequest(){
        val formatter= JsonPrinter()
        formatter.setLogConfig(LogConfig.get())
        request<String> {
            mainThread=true//子线程回调
            contextDetection=false//不检测生命周期
            lifeCycle{
                when(it){
                    RequestLifeCycle.START->progressBar.visibility= View.VISIBLE
                    RequestLifeCycle.FINISH->progressBar.visibility= View.GONE
                }
            }
            get {
                url="v1/weather/query?"
                params= mapOf("key" to APP_KEY,"city" to "通州","province" to "北京")
            }
            map{
                //延持时间,检测上下文是否存在
                SystemClock.sleep(1*1000)
                it
            }
            success {
                //这里测试回调线程信息
                JLog.i("${Thread.currentThread().name} $it")
            }
            failed{
                it.printStackTrace()
            }
        }
    }
}
