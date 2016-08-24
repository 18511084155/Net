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
                setRawName("net_config").
                setRequestUrl("http://www.weixinkd.com").
                setOnRequestListener(new RequestListener());
    }
}
