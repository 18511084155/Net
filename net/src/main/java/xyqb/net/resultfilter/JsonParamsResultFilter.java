package xyqb.net.resultfilter;

import android.text.TextUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by cz on 8/23/16.
 */
public class JsonParamsResultFilter extends ResultFilter {

    @Override
    public HashMap<String, String> result(String result) {
        HashMap<String, String> params = new HashMap<>();
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject json = new JSONObject(result);
                Iterator<String> iKey = json.keys();
                while (iKey.hasNext()) {
                    String key = iKey.next();
                    Object value = json.opt(key);
                    if (JSONObject.NULL.equals(value)) {
                        value = null;
                    }
                    if (null != value && !TextUtils.isEmpty(value.toString())) {
                        params.put(key, value.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return params;
    }
}
