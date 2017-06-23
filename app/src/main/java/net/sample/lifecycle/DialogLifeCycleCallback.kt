package net.sample.lifecycle

import android.app.Dialog
import cz.netlibrary.callback.LifeCycleCallback
import cz.netlibrary.request.RequestLifeCycle

/**
 * Created by cz on 2017/6/8.
 */
class DialogLifeCycleCallback(val dialog: Dialog): LifeCycleCallback {

    override fun call(lifeCycle: RequestLifeCycle) {
        when(lifeCycle){
            RequestLifeCycle.START ->dialog.show()
            RequestLifeCycle.AFTER_FAILED, RequestLifeCycle.AFTER_CALL ->dialog.dismiss()
        }
    }

}