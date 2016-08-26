package xyqb.net.callback;

import xyqb.net.model.HttpResponse;
import xyqb.net.model.RequestItem;

/**
 * Created by cz on 8/23/16.
 */
public interface OnRequestResultListener {

    void onSuccess(HttpResponse response,RequestItem item,String url);

    void onFailed(Exception e,RequestItem item,String url);
}
