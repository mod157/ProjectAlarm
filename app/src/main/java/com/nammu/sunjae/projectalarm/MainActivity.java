package com.nammu.sunjae.projectalarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.recycler_alarmList)
    RecyclerView recyclerView;
    private  Realm realm = null;
    private ArrayList<AlarmItem> items = new ArrayList<>();
    private RealmResults<AlarmData> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        realm = RealmDB.RealmInit(this);
        PermissionCheck();
        DataView();
    }
    @Override
    protected void onResume(){
        super.onResume();

    }
    private void PermissionCheck(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Log.e("#### 위치허가","OK");
            }
            @Override
            public void onPermissionDenied(ArrayList<String> arrayList) {
                Log.e("#### 위치허가","NO");
            }
        };
        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("접근 권한이 필요합니다.")
                .setDeniedMessage("권한설정을 하지 않으면 제한된 정보만 이용 가능합니다.")
                .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .setPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }
    private void ContentProvjer(){

    }
    private RealmResults<AlarmData> getList(){
        return realm.where(AlarmData.class).findAllSorted("time");
    }

    private void DataView() {
        dataList = getList();
        if(dataList.size() != 0) {
            Log.e("#######RecyclerView", "SET : " + dataList.toString());
            //알람을 초기화
            Intent alarmset = new Intent("Alarm_Call");
            sendBroadcast(alarmset);
            for (int i = 0; i < dataList.size(); i++) {
                AlarmItem item = new AlarmItem();
                item.setDays(DaysTrans(dataList.get(i).getDays()));
                item.setTime(dataList.get(i).getTime());
                items.add(item);
            }
            AlarmAdapter adapter = new AlarmAdapter(items, R.layout.layout_item, MainActivity.this);
            recyclerView.setAdapter(adapter);
        }else{
            Log.e("##### RecyclerView", "SET : 0 ");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.actions_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent detailMove = new Intent(MainActivity.this, DetailActivity.class);
        detailMove.putExtra("action","create");
        startActivity(detailMove);
        return super.onOptionsItemSelected(item);
    }

    public static boolean[] DaysTrans(String str){
        boolean[] days = new boolean[7];
        String[] dayStrArray = str.split("-");
        for(int i = 0 ; i < dayStrArray.length; i++){
            days[i] = (Integer.parseInt(dayStrArray[i])!=0);
        }
        return days;
    }
}
