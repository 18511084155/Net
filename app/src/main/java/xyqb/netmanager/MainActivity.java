package xyqb.netmanager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import xyqb.net.HttpRequest;
import xyqb.net.callback.OnRequestFailedListener;
import xyqb.net.callback.OnRequestSuccessListener;
import xyqb.net.exception.HttpException;
import xyqb.net.model.HttpResponse;
import xyqb.net.resultfilter.JsonParamsResultFilter;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView content= (TextView) findViewById(R.id.tv_content);
        findViewById(R.id.btn_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content.setText(null);
//                post json
//                HttpRequest.obtain(NetConfig.HOTEL_ORDER_PAY).
//                        addStringEntity("{\"hotelPhone\":\"021-64471372\",\"hotelName\":\"源涞国际酒店式公寓(测试)\",\"hotelAddress\":\"徐汇区虹桥路168弄东方曼哈顿5号-1703(近文定路\\/1,9,11号线徐家汇站旁)\",\"roomName\":\"单人间(无窗)\",\"roomTypeId\":\"1024\",\"ratePlanId\":\"415940\",\"arrivalDate\":\"2016-9-5\",\"departureDate\":\"2016-9-6\",\"numberOfRooms\":1,\"numberOfCustomers\":1,\"totalPrice\":174,\"orderRooms\":[{\"customers\":[{\"name\":\"eyy\"}]}],\"contact\":{\"name\":\"et\",\"phone\":\"\"}}").
//                        setResultFilter(new JsonParamsResultFilter()).
//                        setOnRequestSuccessListener(new OnRequestSuccessListener<HashMap<String, String>>() {
//                            @Override
//                            public void onSuccess(HttpResponse result, HashMap<String, String> params) {
//                                Log.e(TAG,result.result);
//                                content.setText(result.result);
//                            }
//                        }).setOnRequestFailedListener(new OnRequestFailedListener() {
//                    @Override
//                    public void onFailed(int code, HttpException e) {
//                        Log.e(TAG,e.getMessage());
//                        content.setText(e.getMessage());
//                    }
//                }).call();

                // APP ID
                final String APP_ID = "0002";
                // APP KEY
                final String APP_KEY = "lkb010203#$%";
                //post
                String timeStamp = String.valueOf(System.currentTimeMillis());
                String tokenValue=md5((new StringBuffer()
                        .append("timeunit=").append(timeStamp)
                        .append("appkey=").append(APP_KEY)).toString());



                String userId="15101604692";
                String feedbackValue="abcdefghikjlmn";
                String token="0039615f-0bdf-4f8e-9851-6c99c89b224f";
                HttpRequest.obtain(NetConfig.USER_SUGGESTION, userId, feedbackValue, userId, userId, APP_ID, timeStamp, tokenValue).
                        addHeader("X-Auth-Token", token).
                        addHeader("X-Requested-With", "XMLHttpRequest").
                        addHeader("Content-Type", "application/json").
                        setResultFilter(new JsonParamsResultFilter()).
                        setOnRequestSuccessListener(new OnRequestSuccessListener<HashMap<String, String>>() {
                            @Override
                            public void onSuccess(HttpResponse result, HashMap<String, String> params) {
                                Log.e(TAG, result.result);
                                content.setText(result.result);
                            }
                        }).setOnRequestFailedListener(new OnRequestFailedListener() {
                    @Override
                    public void onFailed(int code, HttpException e) {
                        Log.e(TAG,e.getMessage());
                        content.setText(e.getMessage());
                    }
                }).call();

            }
        });

    }

    public String md5(String content) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        md5.update(content.getBytes());
        byte[] domain = md5.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        // converting domain to String
        for (int i = 0; i < domain.length; i++) {
            if (Integer.toHexString(0xFF & domain[i]).length() == 1) {
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & domain[i]));
            } else
                md5StrBuff.append(Integer.toHexString(0xFF & domain[i]));
        }
        return md5StrBuff.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
