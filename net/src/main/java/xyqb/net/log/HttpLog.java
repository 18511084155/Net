package xyqb.net.log;

import android.util.Log;

import xyqb.net.callback.OnPrintHttpLogListener;

/**
 * Created by cz on 8/26/16.
 */
public class HttpLog {
    private static final String TAG = "HttpLog";
    private static OnPrintHttpLogListener printHttpLogListener;
    private static boolean debug;


    public static void setPrintHttpLogListener(OnPrintHttpLogListener printHttpLogListener) {
        HttpLog.printHttpLogListener = printHttpLogListener;
    }

    public static void setDebug(boolean debug) {
        HttpLog.debug = debug;
    }

    public static void d(String log) {
        if (debug) {
            Log.e(TAG, log);
            if(null!=printHttpLogListener){
                printHttpLogListener.onPrint(TAG,log);
            }
        }
    }
}
