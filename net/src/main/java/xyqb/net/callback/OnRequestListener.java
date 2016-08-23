package xyqb.net.callback;

import java.util.HashMap;

/**
 * Created by cz on 8/23/16.
 */
public interface OnRequestListener {
    /**
     *  global add header listener
     */
    HashMap<String,String> requestHeaderItems();

    /**
     * global add request extra items
     * @return
     */
    HashMap<String,String> requestExtraItems();
}
