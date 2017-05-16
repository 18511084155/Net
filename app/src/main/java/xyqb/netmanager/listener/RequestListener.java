package xyqb.netmanager.listener;

import java.util.HashMap;

import xyqb.net.callback.OnRequestListener;

/**
 * Created by Administrator on 2016/8/23.
 */
public class RequestListener implements OnRequestListener {
    @Override
    public HashMap<String, String> requestHeaderItems() {
        HashMap<String,String> header=new HashMap<>();
//                stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
//
//                stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        return header;
    }

    @Override
    public HashMap<String, String> requestExtraItems() {
        return null;
    }
}
