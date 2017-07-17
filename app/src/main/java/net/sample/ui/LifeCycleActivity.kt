package net.sample.ui

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import cz.netlibrary.request
import cz.netlibrary.request.RequestLifeCycle
import kotlinx.android.synthetic.main.activity_context.*
import net.sample.ProgressDialogLifeCycle
import net.sample.R
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class LifeCycleActivity : AppCompatActivity() {
    val APP_KEY ="ab54e19d080"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_life_cycle)
        title=intent.getStringExtra("title")
        toolBar.title = title
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBar.setNavigationOnClickListener{ finish() }

        requestButton.onClick {
            request<String> {
                get {
                    url="v1/weather/query?"
                    params= mapOf("key" to APP_KEY,"city" to "通州","province" to "北京")
                }
                //设置一个动态的回调器
                lifeCycle{
                    when(it){
                        RequestLifeCycle.START->progressBar.visibility= View.VISIBLE
                        RequestLifeCycle.FINISH->progressBar.visibility= View.GONE
                    }
                }
                //加入一个对话框回调器,以上,与这个接口回调器,二选一即可
                lifeCycleItem(ProgressDialogLifeCycle(this@LifeCycleActivity,"加载中"))
                map{
                    //延持时间,检测上下文是否存在
                    SystemClock.sleep(1*1000)
                    it
                }
                success { toast("请求并回调成功!") }
                failed {
                    contentView.text=it.message
                }
                noNetWork(this@LifeCycleActivity::noNetworkDialog)
            }
        }
    }
    fun noNetworkDialog(){
        AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.net_error_info)
                .setPositiveButton(android.R.string.ok)
                        { _, _ ->
                            if (android.os.Build.VERSION.SDK_INT > 10) {
                                // 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                                startActivity(Intent(Settings.ACTION_SETTINGS))
                            } else {
                                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                            }
                        }.show()
    }
}
