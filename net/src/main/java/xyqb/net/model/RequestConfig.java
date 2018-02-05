package xyqb.net.model;

import java.io.File;

import xyqb.net.callback.OnApplyRequestItemListener;
import xyqb.net.callback.OnClientCreateCallback;
import xyqb.net.callback.OnRequestListener;
import xyqb.net.callback.OnRequestResultListener;

/**
 * Created by cz on 8/23/16.
 */
public class RequestConfig {
    public String rawName;
    public String path;
    public String url;
    public int connectTimeout;
    public int readTimeout;
    public int writeTimeout;
    public File cachedFile;
    public long maxCacheSize;
    public boolean retryOnConnectionFailure;
    public OnClientCreateCallback clientCreateCallback;
    public OnRequestListener listener;
    public OnApplyRequestItemListener applyListener;
    public OnRequestResultListener requestResultListener;

    public RequestConfig() {
    }
}
