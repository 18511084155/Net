package xyqb.net.xml;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import xyqb.library.XmlAttribute;
import xyqb.library.XmlElement;
import xyqb.library.config.XmlReaderBase;
import xyqb.net.model.RequestItem;

import static android.content.ContentValues.TAG;

/**
 * Created by cz on 8/23/16.
 */
public class RequestConfigReader  extends XmlReaderBase<HashMap<String,RequestItem>> {
    @Override
    public HashMap<String, RequestItem> readXmlConfig(XmlElement rootElement) {
        HashMap<String,RequestItem> requestItems=new HashMap<>();
        if(null!=rootElement){
            ArrayList<XmlElement> children = rootElement.getChildren();
            for(XmlElement element:children){
                ArrayList<XmlAttribute> attributes = element.getAttributes();
                RequestItem requestItem = new RequestItem();
                Class<RequestItem> clazz = RequestItem.class;
                for(XmlAttribute attribute:attributes){
                    String value = attribute.value;
                    if("params".equals(attribute.name)){
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
                            Log.e(TAG,"name:"+attribute.name+" value:"+value);
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
