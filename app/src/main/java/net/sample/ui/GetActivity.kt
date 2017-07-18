package net.sample.ui

import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.cz.loglibrary.LogConfig
import com.cz.loglibrary.impl.JsonPrinter
import cz.netlibrary.callback.RequestSuccessCallback
import cz.netlibrary.request
import cz.netlibrary.request.RequestLifeCycle
import cz.netlibrary.syncRequest
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
        //同步
//        requestButton.onClick {
//            Executors.newSingleThreadExecutor().execute { syncRequest() }
//        }
        //异步
        requestButton.onClick { request()}
        cleanButton.onClick { contentView.text=null }
    }

    fun request(){
        val formatter=JsonPrinter()
        formatter.setLogConfig(LogConfig.get())
        request<String> {
            get {
                url="v1/weather/query?"
                params= mapOf("key" to APP_KEY,"city" to "通州","province" to "北京")
            }
            lifeCycle{
                when(it){
                    RequestLifeCycle.START->progressBar.visibility= View.VISIBLE
                    RequestLifeCycle.FINISH->progressBar.visibility= View.GONE
                }
            }
            map{
                //延持时间,检测上下文是否存在
                SystemClock.sleep(2*1000)
                it
            }
            successItem=object :RequestSuccessCallback<String>{
                override fun onSuccess(item: String) {
                    contentView.append("Done\n")
                }
            }
            success{
                contentView.append(formatter.format(it).reduce { acc, s -> acc+s })
            }
            failed {
                contentView.text=it.message
            }
        }
    }

    fun syncRequest(){
        val formatter=JsonPrinter()
        formatter.setLogConfig(LogConfig.get())
        syncRequest<String> {
            get {
                url="v1/weather/query?"
                params= mapOf("key" to APP_KEY,"city" to "通州","province" to "北京")
            }
            lifeCycle{
                when(it){
                    RequestLifeCycle.START->progressBar.visibility= View.VISIBLE
                    RequestLifeCycle.FINISH->progressBar.visibility= View.GONE
                }
            }
            map{
                //延持时间,检测上下文是否存在
                SystemClock.sleep(2*1000)
                it
            }
            success {
                contentView.append(formatter.format(it).reduce { acc, s -> acc+s })
            }
            failed {
                contentView.text=it.message
            }
        }
    }
}
