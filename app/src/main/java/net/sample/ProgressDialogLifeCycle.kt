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
    var condition: (()->Boolean)?=null
    init {
        dialog.setMessage(text)
    }

    override fun call(lifeCycle: RequestLifeCycle) {
        val condition=condition;
        if(null==condition||condition.invoke()){
            when(lifeCycle){
                RequestLifeCycle.START->dialog.show()
                RequestLifeCycle.FINISH->dialog.dismiss()
            }
        }
    }

    fun condition(condition: ()->Boolean){
        this.condition=condition
    }

}