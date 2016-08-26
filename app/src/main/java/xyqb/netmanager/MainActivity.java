package xyqb.netmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

import xyqb.net.HttpRequest;
import xyqb.net.callback.OnRequestFailedListener;
import xyqb.net.callback.OnRequestSuccessListener;
import xyqb.net.exception.HttpException;
import xyqb.net.model.HttpResponse;
import xyqb.net.resultfilter.JsonParamsResultFilter;

public class MainActivity extends AppCompatActivity {

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
                HttpRequest.obtain(NetConfig.HOTEL_DETAIL,"2016-8-22","2016-8-26").
                        addPathValue(12).
                        setResultFilter(new JsonParamsResultFilter()).
                        setOnRequestSuccessListener(new OnRequestSuccessListener<HashMap<String, String>>() {
                            @Override
                            public void onSuccess(HttpResponse result, HashMap<String, String> params) {
                                Log.e(TAG,result.result);
                                content.setText(result.result);
                            }
                        }).setOnRequestFailedListener(new OnRequestFailedListener() {
                    @Override
                    public void onFailed(int code, HttpException e) {
                        Log.e(TAG,e.getMessage());
                        content.setText(e.getMessage());
                    }
                }).call(null);
            }
        });

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
