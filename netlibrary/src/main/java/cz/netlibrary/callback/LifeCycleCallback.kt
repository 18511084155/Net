package cz.netlibrary.callback

import cz.netlibrary.request.RequestLifeCycle

/**
 * Created by cz on 2017/6/8.
 */
interface LifeCycleCallback {
    fun call(lifeCycle: RequestLifeCycle):Unit
}