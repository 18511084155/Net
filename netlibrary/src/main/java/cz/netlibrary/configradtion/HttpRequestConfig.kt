package cz.netlibrary.configradtion

import cz.netlibrary.model.RequestConfig
import java.io.File

/**
 * Created by cz on 2017/6/7.
 */
class HttpRequestConfig {
    var url: String? = null
    var connectTimeout: Int = 16*1000
    var readTimeout: Int = 16*1000
    var writeTimeout: Int = 16*1000
    var cachedFile: File? = null
    var maxCacheSize: Long = 10*1024*1024
    var retryOnConnectionFailure=false
    var extrasParams:()->List<Pair<String,String>>?={null}
    var extrasHeader:()->List<Pair<String,String>>?={null}
    var pre: (RequestConfig)->String? = {null}
    var networkInterceptor:(RequestConfig)->Boolean={true}
    var requestResult: ((RequestConfig, Int, String?, Long) -> Unit)? = null
    var log=false
}