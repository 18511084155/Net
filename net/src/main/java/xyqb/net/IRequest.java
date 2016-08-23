package xyqb.net;

import rx.Observable;
import xyqb.net.model.HttpResponse;
import xyqb.net.model.RequestItem;
import xyqb.net.resultfilter.ResultFilter;

/**
 * Created by cz on 8/23/16.
 */
public interface IRequest<T> {
    int REQUEST_ERROR=-100;
    String GET="get";
    String POST="post";

    Observable<HttpResponse> call(Object obj,RequestItem item,Object... values);

    void setResultFilter(ResultFilter<T> resultFilter);
}
