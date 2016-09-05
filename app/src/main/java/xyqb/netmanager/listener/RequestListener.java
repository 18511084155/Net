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
        header.put("version", "quantgroup");
        header.put("appChannel", "4.9.0.0");

        header.put("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6IjEzODEwMjY2Njg1IiwiaWF0IjoxNDcyNzI0ODA0LCJhdWQiOiJBbmRyb2lkIn0.eemwZzouGvPn0Y-_liF3DcvdSgik5WnCdNt1GWV5Hww");
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
