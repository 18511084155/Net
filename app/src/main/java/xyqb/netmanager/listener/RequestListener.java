package xyqb.netmanager.listener;

import java.util.HashMap;

import xyqb.net.callback.OnRequestListener;

/**
 * Created by Administrator on 2016/8/23.
 */
public class RequestListener implements OnRequestListener {
    @Override
    public HashMap<String, String> requestHeaderItems() {
        return null;
    }

    @Override
    public HashMap<String, String> requestExtraItems() {
        HashMap<String,String> items=new HashMap<>();
        items.put("a","b");
        items.put("b","c");
        return items;
    }
}
