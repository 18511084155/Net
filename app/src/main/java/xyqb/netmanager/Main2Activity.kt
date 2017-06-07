package xyqb.netmanager

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.netlibrary.model.RequestItem
import cz.netlibrary.request
import cz.netlibrary.requestString
import xyqb.netmanager.NetWorkConfigration.action1

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        request<RequestItem>(action1) {
            success {
                it.action
            }
        }
        requestString {
            get {
                url="abcd"
                params= mutableListOf("1".to("2"))
            }
            success {
            }
            failed {
            }
            noNetWork(true){}

        }
    }
}
