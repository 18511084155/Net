package xyqb.netmanager

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.netlibrary.cancelRequest
import cz.netlibrary.request

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        request<Boolean>(NetWorkPrefs.action1) {
            mainThread=true
            post {
                url="login/"
            }
            map{ it as Boolean}

            success {

            }

            failed {

            }

            noNetWork {

            }
        }
        cancelRequest("login")
    }
}
