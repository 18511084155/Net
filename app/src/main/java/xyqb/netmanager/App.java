package xyqb.netmanager;

import android.app.Application;

import xyqb.net.NetManager;
import xyqb.netmanager.listener.RequestListener;

/**
 * Created by Administrator on 2016/8/23.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetManager.getInstance().
                setHttpDebug(true).
                setRawName("net_config").
                setRequestUrl("http://192.168.192.214:9000/").
                setOnRequestListener(new RequestListener());
    }
}
