package com.nammu.sunjae.projectalarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.android.volley.Request.Method.GET;

/**
 * Created by SunJae on 2017-01-26.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private Context context;
    private Realm realm;
    private String view_Title;
    private String locationAddress = "";
    private LocationManager lm;
    double[] grid = new double[2];

    RequestQueue req;
    int delay;
    private JSONObject jsonWeatherData = new JSONObject();
    int[] iconList = new int[]{R.drawable.summer, R.drawable.cloud_2, R.drawable.sky, R.drawable.cloud, R.drawable.rain, R.drawable.weather};
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        this.context = context;
        //시스템이 상태가 부팅완료인지 확인
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            //실행할 액티비티
            Intent actionintent = new Intent(context, ViewActivity.class);
            actionintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(actionintent);
        }

        if (action.equals("Alarm_Delete")) {

            RealmResults<AlarmData> results = realm.where(AlarmData.class).findAllSorted("time");
            if (results.size() == 0)
                cancelAlram(pendingIntent());
            else {
                Intent call = new Intent("Alarm_Call");
                context.sendBroadcast(call);
            }
        }

        if (action.equals("Alarm_Call")) {
            //알람시간대 1분전에 위치 탐색, 날씨 정보 제공
            Log.e("##### BroadCast Call", "Alarm Call OK");
            delay = setAlarm_Delay();
            Log.e("#### Checking ", "" + GpsServiceChecking());
            if (GpsServiceChecking()) {
                Log.e("####### GeoPoint","OK");
                Thread thread = new Thread (new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getLocation();
                    }
                });
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(thread,delay-60000);
            }else {
                setAlram(pendingIntent(), delay);
            }
        }
    }

    /*
     알람 설정
    * */
    private int setAlarm_Delay() {
        int delay_time;
        realm = RealmDB.RealmInit(context);
        Calendar calendar = Calendar.getInstance();
        String date_str = String.valueOf(calendar.getTime());
        String[] splitdate = date_str.split(" ");
        //시 초
        int day_num = getDay(splitdate[0]);
        Date date = getDate(splitdate[3].substring(0, 5));
        RealmResults<AlarmData> results = realm.where(AlarmData.class).findAllSorted("time");
        delay_time = minAlarm(day_num, results, date);
        String date_sub = calendar.getTime().toString();
        Log.e("##### 딜레이 시간",Integer.parseInt(date_sub.split(" ")[3].substring(6,7))*10000 + Integer.parseInt(date_sub.split(" ")[3].substring(7,8))*1000+ "");
        delay_time -= (Integer.parseInt(date_sub.split(" ")[3].substring(6,7))*10000 + Integer.parseInt(date_sub.split(" ")[3].substring(7,8))*1000)/2;
        Log.e("##### 다음 알람 : " + (((delay_time / 1000) / 60) / 60) / 24 + "일 " + (((delay_time / 1000) / 60) / 60) % 24 + "시간 " + ((delay_time / 1000) / 60) % 60 + "분 " + (delay_time / 1000) % 60 + "초", calendar.getTime().toString());
        return delay_time;
    }

    //제일 가까운 Alarm 설정
    private int minAlarm(int day_num, RealmResults<AlarmData> results, Date date) {
        int delay_time;
        boolean[] days;
        for (int count = 0; count < 7; day_num++, count++) {
            for (int i = 0; i < results.size(); i++) {
                days = MainActivity.DaysTrans(results.get(i).getDays());
                Date db_Date = getDate(results.get(i).getTime());
                //오늘 요일
                if (days[day_num]) {
                    //요일 증가마다 시간 추가
                    if (((count * 86400) * 1000 + (int) (db_Date.getTime() - date.getTime())) >= 10000) {
                        delay_time = (count * 86400) * 1000 + (int) (db_Date.getTime() - date.getTime());
                        view_Title = results.get(i).getTitle();
                        return delay_time;
                    }
                }
            }
            //로테이션
            if (day_num >= 6) {
                day_num = 0;
            }
        }
        return 0;
    }

    private Date getDate(String date_str) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        java.util.Locale.getDefault();
        Date date = null;
        try {
            date = dateFormat.parse(date_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private int getDay(String day) {
        int day_num = 0;
        switch (day) {
            case "Mon":
                day_num = 0;
                break;
            case "Tue":
                day_num = 1;
                break;
            case "Wed":
                day_num = 2;
                break;
            case "Thu":
                day_num = 3;
                break;
            case "Fri":
                day_num = 4;
                break;
            case "Sat":
                day_num = 5;
                break;
            case "Sun":
                day_num = 6;
                break;
        }
        return day_num;
    }

    private void cancelAlram(PendingIntent pendingIntent){
        AlarmManager manager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }


    /*
    Alarm Setting 메소드
    * */
    private PendingIntent pendingIntent() {
        Log.e("#### PendIntent ", locationAddress);
        Intent i = new Intent(context, AlarmActivity.class);
        i.putExtra("ViewTitle", view_Title);
        i.putExtra("LocationAddress", locationAddress);
        if(GpsServiceChecking()&&jsonWeatherData == null){
            Intent restart = new Intent("Alarm_Call");
            context.sendBroadcast(restart);
        }
        i.putExtra("jsonWeather",jsonWeatherData.toString());
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        return pi;
    }
    private void setAlram(PendingIntent pendingIntent, int delay) {
        Log.e("######## AlarmSet","OK");
        AlarmManager manager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            manager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        }
    }

    /*
    * 현재 경위도 정보
    * */
    public void getLocation() {
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider;
        Criteria c = new Criteria();

        //제일 좋은 장치를 추출
        provider = lm.getBestProvider(c, true);

        //장치에서 사용가능한 목록 추출
        if (provider == null || !lm.isProviderEnabled(provider)) {
            // 모든 장치 목록
            List<String> list = lm.getAllProviders();
            for (int i = 0; i < list.size(); i++) {
                //장치 이름 하나 얻기
                String temp = list.get(i);

                //사용 가능 여부 검사
                if (lm.isProviderEnabled(temp)) {
                    provider = temp;
                    break;
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //현재 위치 정보
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        Location location = null;
        if(location == null) {
            //마지막 위치 정보
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                location = lm.getLastKnownLocation(provider);
                if (location != null)
                    locationAddress = getAddress(location.getLatitude(), location.getLongitude());
            }
        }
        Log.e("##### getLocation", "OK :" +locationAddress);
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            if(location!=null)
                locationAddress = getAddress(lat, lng);
            Log.e("####### Location :" + locationAddress, lat + " : " + lng);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e("###### listener","OK");
        }

        public void onProviderEnabled(String provider) {
            Log.e("###### listener","OK");
        }

        public void onProviderDisabled(String provider) {
            Log.e("###### listener","OK");
        }
    };

    public String getAddress(double lat, double lng) {
        Log.e("##### getAddress", "OK");
        String address = null;
        //위치정보를 활용하기 위한 구글 API 객체
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        //주소 목록을 담기 위한 HashMap
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (list == null)
            return null;

        if (list.size() > 0) {
            Address addr = list.get(0);
            address = addr.getCountryName() +  " " + addr.getLocality() + " " + addr.getThoroughfare() + " " + addr.getFeatureName();
            double[] grid = kmcTrans.dfs_xy_conv(lat, lng);
            weatherSearch(grid[0], grid[1]);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        //종료
        try {
            lm.removeUpdates(locationListener);
        }catch(NullPointerException e) {
            e.printStackTrace();
        }

        Log.e("##### getAddress", address);
        return address;
    }

    private void weatherSearch(Double gridX, Double gridY){
        Log.e("##### WeatherSeacrch", "OK");
        String url = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx="+gridX+"&gridy="+gridY;
        req = NetworkSingle.getInstace(context).getReq();
        StringRequest stringArrayRequest = new StringRequest(GET, url, listenerJson(), listenError());
        req.add(stringArrayRequest);
    }

    private Response.Listener listenerJson() {
        Log.e("#### XMLLstener", "OK");
        return new Response.Listener() {
            @Override
            public void onResponse(Object response) {

                InputSource is = new InputSource(new StringReader(response.toString()));
                try {
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                    XPath xpath = XPathFactory.newInstance().newXPath();

                    NodeList nodeList = (NodeList) xpath.evaluate("//data", document, XPathConstants.NODESET);
                    //item get 내일 모레까지 얻을 수 있음
                    String[] s = nodeList.item(0).getTextContent().split("\n");
                    jsonWeatherData.put("weather_temp", s[3]); //온도
                    jsonWeatherData.put("weather_wf", s[8]); // 날씨
                    jsonWeatherData.put("weather_pop", s[10]); // 강수확률
                    switch (s[8].trim()){
                        case "맑음":
                            jsonWeatherData.put("weather_img",iconList[0]);
                            break;
                        case "구름 조금":
                            jsonWeatherData.put("weather_img",iconList[1]);
                            break;
                        case "구름 많음":
                            jsonWeatherData.put("weather_img",iconList[2]);
                            break;
                        case "흐림":
                            jsonWeatherData.put("weather_img",iconList[3]);
                            break;
                        case "비":
                            jsonWeatherData.put("weather_img",iconList[4]);
                            break;
                        case "눈/비":
                        case "눈":
                            jsonWeatherData.put("weather_img",iconList[5]);
                            break;
                    }

                    Log.e("###### NodeData : " + 0,"\n" + "온도 : " + s[3].trim() + "\n 날씨 : " + s[8].trim() + "\n 강수확률 : " + s[10].trim() + "\n 이미지 :" +jsonWeatherData.getString("weather_img"));
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                setAlram(pendingIntent(), delay-62000);
            }
        };
    }

    private Response.ErrorListener listenError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Listener", "error" + error);
            }
        };
    }



    //GPS 설정 체크
    private boolean GpsServiceChecking() {
        String gps = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if ((gps.matches(".*gps.*") )) {
            Log.e("##### GPS","OK");
            return true;
        }
        return false;
    }
}
