package xyqb.net.util;

import android.text.TextUtils;

import java.util.Map;

import xyqb.net.model.RequestConfig;
import xyqb.net.model.RequestItem;

/**
 * Created by cz on 10/28/16.
 */

public class NetUtils {

    public static String getParamValue(Map<String,String> params){
        StringBuilder builder=new StringBuilder();
        if(null!=params){
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if(null!=entry.getValue()){
                    builder.append(entry.getKey() + "=" + entry.getValue() +"&");
                }
            }
            if(0!=builder.length()){
                builder.deleteCharAt(builder.length()-1);
            }
        }
        return builder.toString();
    }

    public static String getCookieValue(Map<String,String> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        if(null!=cookies){
            for(Map.Entry<String,String> entry:cookies.entrySet()){
                if(null!=entry.getValue()){
                    cookieHeader.append(entry.getKey() + '=' + entry.getValue() + ";");
                }
            }
            if(0!=cookieHeader.length()){
                cookieHeader.deleteCharAt(cookieHeader.length()-1);
            }
        }
        return cookieHeader.toString();
    }

    public static String getRequestUrl(RequestConfig config,RequestItem item){
        //此设计在应用requestItem之前,可以全局拦截,修改信息
        if(null!=config.applyListener){
            config.applyListener.onApplyItem(item);
        }
        String absoluteUrl;
        if(!item.url.startsWith("http")){
            absoluteUrl=config.url+item.url;
        } else if(!TextUtils.isEmpty(item.dynamicUrl)){
            absoluteUrl=item.dynamicUrl+item.url;
        } else {
            absoluteUrl=item.url;
        }
        return absoluteUrl;
    }
}
