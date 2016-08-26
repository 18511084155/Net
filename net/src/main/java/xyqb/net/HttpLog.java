package xyqb.net;

import android.util.Log;

/**
 * Created by cz on 8/26/16.
 */
public class HttpLog {
    private static final String TAG = "HttpLog";
    private static boolean debug;


    public static void setHttpDebug(boolean debug) {
        HttpLog.debug = debug;
    }


    public static void d(String log) {
        if (debug) {
            Log.e(TAG, log);
        }
    }

}
