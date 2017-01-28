package com.nammu.sunjae.projectalarm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        Intent alarmset = new Intent("Alarm_Call");
        sendBroadcast(alarmset);
        Log.e("##### refresh", "OK");
        finish();
    }
    @Override
    protected  void onDestroy(){
        super.onDestroy();
    }
}
