package xyqb.netmanager;

import android.app.Application;

import xyqb.net.NetManager;
import xyqb.net.callback.OnApplyRequestItemListener;
import xyqb.net.model.RequestItem;
import xyqb.netmanager.listener.RequestListener;

/**
 * Created by Administrator on 2016/8/23.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetManager.getInstance().
                setDebug(true).
                setRawName("net_config").
                setRequestUrl("http://192.168.192.214:9000/").
                setOnApplyRequestItemListener(new OnApplyRequestItemListener() {
                    @Override
                    public void onApplyItem(RequestItem item) {
                        if(!item.url.startsWith("http")){
                            item.url="http://openapi.quantgroup.cn/"+item.url;
                        }
                    }
                }).
                setOnRequestListener(new RequestListener());
    }
}
