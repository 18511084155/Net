package xyqb.netmanager.prefs

import cz.netlibrary.model.Configuration
import cz.netlibrary.model.RequestMethod

/**
 * Created by cz on 2017/6/7.
 */
object NetWorkPrefs {
    val action1="请求登录"
    init {
        Configuration.init {
            item {
                action = action1
                method = RequestMethod.get
                info = "请求1"
                url = ""
                params = arrayOf("", "", "")
            }
            item {

            }

        }
    }

}

