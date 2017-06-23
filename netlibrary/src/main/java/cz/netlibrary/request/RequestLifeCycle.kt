package cz.netlibrary.request

/**
 * Created by cz on 2017/6/8.
 */
enum class RequestLifeCycle {
    //请求开始
    START,
    //请求回调开始
    BEFORE_CALL,
    //请求回调结束
    AFTER_CALL,
    //请求异常开始
    BEFORE_FAILED,
    //请求异常结束
    AFTER_FAILED,
    //任务生命周期检测未通过
    CANCEL,
    //请求结束
    FINISH
}