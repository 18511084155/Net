package xyqb.net.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cz on 8/23/16.
 */
public class HttpResponse {
    public String result;
    public Map<String,String> headers;

    public HttpResponse() {
        this.headers=new HashMap<>();
    }
}
