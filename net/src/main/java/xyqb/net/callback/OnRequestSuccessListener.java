package xyqb.net.callback;

import xyqb.net.model.HttpResponse;

/**
 * Created by cz on 8/23/16.
 */
public interface OnRequestSuccessListener<T> {
    void onSuccess(HttpResponse result,T t);
}
