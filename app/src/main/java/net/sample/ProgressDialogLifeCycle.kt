package net.sample

import android.app.ProgressDialog
import android.content.Context
import cz.netlibrary.callback.LifeCycleCallback
import cz.netlibrary.request.RequestLifeCycle

/**
 * Created by cz on 2017/6/23.
 */
class ProgressDialogLifeCycle(context: Context,text:String): LifeCycleCallback{
    val dialog=ProgressDialog(context)
    init {
        dialog.setMessage(text)
    }

    override fun call(lifeCycle: RequestLifeCycle) {
        when(lifeCycle){
            RequestLifeCycle.START->dialog.show()
            RequestLifeCycle.CANCEL,
            RequestLifeCycle.AFTER_CALL,
            RequestLifeCycle.AFTER_FAILED->dialog.dismiss()
        }
    }

}