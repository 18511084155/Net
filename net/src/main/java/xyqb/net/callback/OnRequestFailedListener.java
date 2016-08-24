package xyqb.net.callback;

import xyqb.net.exception.HttpException;

/**
 * Created by cz on 8/23/16.
 */
public interface OnRequestFailedListener {
    void onFailed(int code,HttpException e);
}
