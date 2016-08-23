package xyqb.net.xml;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import xyqb.net.NetManager;
import xyqb.net.model.RequestConfig;
import xyqb.net.model.RequestItem;
import xyqb.xml.XmlAttribute;
import xyqb.xml.XmlElement;
import xyqb.xml.XmlHelper;

/**
 * Created by cz on 8/23/16.
 */
public class RequestConfigReader {
    private final RequestConfig requestConfig;
    private  XmlElement cacheElement;
    private Context appContext;

    public RequestConfigReader(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    private Context getContext() {
        if (appContext == null) {
            try {
                final Class<?> activityThreadClass = NetManager.class.getClassLoader().loadClass("android.app.ActivityThread");
                final Method currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread");
                final Object activityThread = currentActivityThread.invoke(null);
                final Method getApplication = activityThreadClass.getDeclaredMethod("getApplication");
                final Application application = (Application) getApplication.invoke(activityThread);
                appContext = application.getApplicationContext();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return appContext;
    }

    private XmlElement getXmlElement() {
        if(null== cacheElement){
            InputStream inputStream =null;
            Context context = getContext();
            Resources resources = context.getResources();
            if (!TextUtils.isEmpty(requestConfig.path)) {
                try {
                    inputStream = resources.getAssets().open(requestConfig.path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (!TextUtils.isEmpty(requestConfig.rawName)) {
                String packageName = context.getPackageName();
                int resourceId = resources.getIdentifier(requestConfig.rawName, "raw", packageName);
                inputStream = resources.openRawResource(resourceId);
            }
            if(null!=inputStream){
                cacheElement = XmlHelper.parserStream(inputStream);
            }
        }
        return cacheElement;
    }

    public HashMap<String,RequestItem> readerRequestItems(){
        XmlElement rootElement = getXmlElement();
        HashMap<String,RequestItem> requestItems=new HashMap<>();
        if(null!=rootElement){
            ArrayList<XmlElement> children = rootElement.getChildren();
            for(XmlElement element:children){
                ArrayList<XmlAttribute> attributes = element.getAttributes();
                RequestItem requestItem = new RequestItem();
                Class<RequestItem> clazz = RequestItem.class;
                for(XmlAttribute attribute:attributes){
                    String value = attribute.value;
                    if("param".equals(attribute.name)){
                        requestItem.param =value.split("&");
                    } else if("files".equals(attribute.name)){
                        requestItem.files=value.split("&");
                    } else {
                        try {
                            Field field = clazz.getField(attribute.name);
                            field.set(requestItem,attribute.value);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
                requestItems.put(requestItem.action,requestItem);
            }
        }
        return requestItems;
    }

}
