package xyqb.netmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import xyqb.net.HttpRequest;
import xyqb.net.callback.OnRequestFailedListener;
import xyqb.net.callback.OnRequestSuccessListener;
import xyqb.net.exception.HttpException;
import xyqb.net.model.HttpResponse;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpRequest.request(NetConfig.COLUMN_LIST,System.currentTimeMillis()).
                        setOnRequestSuccessListener(new OnRequestSuccessListener() {
                    @Override
                    public void onSuccess(HttpResponse result) {
                        Log.e(TAG,result.result);
                    }
                }).setOnRequestFailedListener(new OnRequestFailedListener() {
                    @Override
                    public void onFailed(HttpException e) {
                        Log.e(TAG,e.getMessage());
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
