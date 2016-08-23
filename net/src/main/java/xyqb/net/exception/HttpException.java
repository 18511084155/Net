package xyqb.net.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cz on 8/23/16.
 */
public class HttpException extends Exception {
    public int code;
    public String result;
    public String message;
    public Map<String,String> headers;

    public HttpException() {
        this.headers=new HashMap<>();
    }
}
