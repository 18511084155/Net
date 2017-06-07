package xyqb.netmanager

import cz.netlibrary.model.Configuration
import cz.netlibrary.model.RequestMethod

/**
 * Created by cz on 2017/6/7.
 */
object NetWorkConfigration{
    val action1="请求登录"
    init {
        Configuration.init({
            action=action1
            method= RequestMethod.get
            info="请求1"
            url=""
        },{
            action=""
            method= RequestMethod.get
            info="请求2"
            url=""
        },{

        },{

        })
    }

}

