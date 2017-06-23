package net.sample.ui

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.cz.loglibrary.LogConfig
import com.cz.loglibrary.impl.JsonPrinter
import cz.netlibrary.request
import cz.netlibrary.request.RequestLifeCycle
import kotlinx.android.synthetic.main.activity_get.*
import net.sample.R
import org.jetbrains.anko.sdk25.coroutines.onClick

class GetActivity : AppCompatActivity() {
    val APP_KEY ="ab54e19d080"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get)
        title=intent.getStringExtra("title")
        toolBar.title = title
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBar.setNavigationOnClickListener{ finish() }
        requestButton.onClick { getRequest() }
        cleanButton.onClick { contentView.text=null }
    }

    fun getRequest(){
        val formatter=JsonPrinter()
        formatter.setLogConfig(LogConfig.get())
        request<String> {
            get {
                url="v1/weather/query?"
                params= mapOf("key" to APP_KEY,"city" to "通州","province" to "北京")
            }
            lifeCycle={
                when(it){
                    RequestLifeCycle.START->progressBar.visibility= View.VISIBLE
                    RequestLifeCycle.FINISH->progressBar.visibility= View.GONE
                }
            }
            map{
                //延持时间,检测上下文是否存在
                SystemClock.sleep(1*1000)
                it
            }
            success {
                contentView.append(formatter.format(it).reduce { acc, s -> acc+s })
            }
            failed {
                contentView.text=it.result
            }
        }
    }
}
