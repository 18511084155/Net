package xyqb.net.callback;

import okhttp3.OkHttpClient;

/**
 * Created by woodys on 2/5/18.
 */
public interface OnClientCreateCallback {
    /**
     *  global add header listener
     */
    void onCallback(OkHttpClient.Builder callback);
}
