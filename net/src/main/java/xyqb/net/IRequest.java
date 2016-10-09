package xyqb.net;

import rx.Observable;
import xyqb.net.model.HttpResponse;
import xyqb.net.model.RequestItem;

/**
 * Created by cz on 8/23/16.
 */
public interface IRequest {
    int REQUEST_NO_NETWORK=-100;
    int REQUEST_NO_CODE=-101;
    int REQUEST_ERROR=-404;
    long MAX_CACHE_SIZE=10*1024*1024;
    String GET="get";
    String POST="post";
    String PUT ="put";

    Observable<HttpResponse> call(String tag,RequestItem item,Object... values);

    void cancel(String tag);
}
