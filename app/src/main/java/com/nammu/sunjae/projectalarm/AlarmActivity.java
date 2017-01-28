package com.nammu.sunjae.projectalarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;

//알람 발생시 뛰워줄 페이지
public class AlarmActivity extends AppCompatActivity {
    @BindView(R.id.tv_View_Time)
    TextView tv_View_Time;

    @BindView(R.id.tv_View_DetailTime)
    TextView tv_View_DetailTime;

    @BindView(R.id.tv_View_Text)
    TextView tv_View_Text;

    @BindView(R.id.tv_View_Location)
    TextView tv_View_Location;
    @BindView(R.id.tv_weather_pop)
    TextView tv_weather_pop;
    @BindView(R.id.tv_weather_temp)
    TextView tv_weather_temp;
    @BindView(R.id.tv_weather_wf)
    TextView tv_weather_wf;
    @BindView(R.id.weather_linear)
    LinearLayout weather_linear;
    @BindView(R.id.img_weather)
    ImageView img_weather;

    @OnLongClick(R.id.img_View_Click)
    public boolean imgClick(View view) {
        finish();
        return true;
    }

    private String title;
    private Vibrator vibe;
    private SoundPool sp;
    private int soundID = 0;
    private String locationAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);
        Intent getView = getIntent();
        title = getView.getStringExtra("ViewTitle");
        locationAddress = getView.getStringExtra("LocationAddress");
        if(!locationAddress.equals("")) {
            try {
                JSONObject json = new JSONObject(getView.getStringExtra("jsonWeather"));
                WeatherInit(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ViewInit();
        Sound();
        Vibrate();
        Log.e("##### Alarm_Activity", locationAddress + " OK");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vibe.cancel();
        sp.stop(soundID);

    }
    private void WeatherInit(JSONObject json_Weather){
        weather_linear.setVisibility(View.VISIBLE);
        try {
            String temp = json_Weather.getString("weather_temp");
            String wf = json_Weather.getString("weather_wf");
            String pop = json_Weather.getString("weather_pop");
            int img = Integer.parseInt(json_Weather.getString("weather_img"));
            tv_weather_temp.setText(temp);
            tv_weather_wf.setText(wf);
            tv_weather_pop.setText(pop);
            img_weather.setImageResource(img);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void Sound() {
        Log.e("#### Sound", "OK");
        sp = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        soundID = sp.load(this, R.raw.mountain_musician, 1);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int soundID, int status) {
                soundPool.play(soundID, 1, 1, 0, -1, 1);
            }
        });
    }

    private void Vibrate() {
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] patten = {0, 1000, 500, 2000, 1000};
        vibe.vibrate(patten, 0);
    }

    private void noti(Context context, String time) {
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.alarm_clock).setTicker("알람").setWhen(System.currentTimeMillis())
                .setContentTitle(time).setContentText(title)
                .setDefaults(Notification.DEFAULT_SOUND).setContentIntent(pendingIntent).setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationmanager.notify(1, builder.build());
        }
    }

    private void ViewInit() {
        Log.e("####### Viewinit", "OK");
        Intent alarmset = new Intent("Alarm_Call");
        sendBroadcast(alarmset);
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        calendar.setTime(date);
        int dayNum = calendar.get(Calendar.DAY_OF_WEEK);
        String day = null;
        switch (dayNum) {
            case 1:
                day = "일";
                break;
            case 2:
                day = "월";
                break;
            case 3:
                day = "화";
                break;
            case 4:
                day = "수";
                break;
            case 5:
                day = "목";
                break;
            case 6:
                day = "금";
                break;
            case 7:
                day = "토";
                break;
        }
        SimpleDateFormat time_Format = new SimpleDateFormat("HH:mm");
        SimpleDateFormat month_Format = new SimpleDateFormat("MM-DD");
        tv_View_Time.setText(time_Format.format(date));
        tv_View_DetailTime.setText((month_Format.format(date).split("-")[0] + "월 " + month_Format.format(date).split("-")[1] + "일 " + day + "요일"));
        tv_View_Text.setText(title);
        tv_View_Location.setText(locationAddress);
        noti(this, time_Format.format(date));

    }

}
