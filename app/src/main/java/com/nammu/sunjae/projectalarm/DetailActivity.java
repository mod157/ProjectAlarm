package com.nammu.sunjae.projectalarm;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static android.R.attr.textColorTertiary;

public class DetailActivity extends AppCompatActivity  {
    @BindView(R.id.layout_days)
    LinearLayout layout_days;
    @BindView(R.id.layout_time)
    LinearLayout layout_time;
    @BindView(R.id.et_detail_text)
    EditText et_text;
    @BindView(R.id.tv_detail_time)
    TextView tv_detail_time;
    @BindView(R.id.tv_day_detail_mon)
    TextView tv_detail_mon;
    @BindView(R.id.tv_day_detail_tue)
    TextView tv_detail_tue;
    @BindView(R.id.tv_day_detail_wednes)
    TextView tv_detail_wednes;
    @BindView(R.id.tv_day_detail_thurs)
    TextView tv_detail_thurs;
    @BindView(R.id.tv_day_detail_fri)
    TextView tv_detail_fri;
    @BindView(R.id.tv_day_detail_satur)
    TextView tv_detail_satur;
    @BindView(R.id.tv_day_detail_sun)
    TextView tv_detail_sun;

    @OnClick({R.id.layout_days, R.id.layout_time})
    public void LayoutClick(View v){
        switch (v.getId()){
            case R.id.layout_days:
                AlertDialogSet();
                break;
            case R.id.layout_time:
                new TimePickerDialog(DetailActivity.this,timeSetListenr,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE), false).show();
                break;
        }
    }
    private Calendar calendar = Calendar.getInstance();
    TimePickerDialog.OnTimeSetListener timeSetListenr =
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    setLabel();
                }
            };
    private Realm realm;
    private boolean[] days = new boolean[7];
    boolean isEdit = false;
    private int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("상세 설정");
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Intent action = getIntent();
        if(action.getStringExtra("action").equals("edit")) {
            isEdit = true;
            position = Integer.parseInt(action.getStringExtra("position"));
            EditAlarm();
        }else
            isEdit = false;
    }

    private void EditAlarm(){
        RealmInit();
        RealmResults<AlarmData> rr = realm.where(AlarmData.class).findAllSorted("time");
        AlarmData ad = rr.get(position);
        tv_detail_time.setText(ad.getTime());
        days = MainActivity.DaysTrans(ad.getDays());
        for(int i = 0; i<days.length; i++){
            TextColorSet(days);
        }
        et_text.setText(ad.getTitle());
    }
    public void RealmInit(){
        realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();
    }

    private void TextColorSet(boolean[] isEditColor){
        TextView[] tv_days = {tv_detail_mon, tv_detail_tue, tv_detail_wednes, tv_detail_thurs, tv_detail_fri, tv_detail_satur, tv_detail_sun};
        for(int i = 0; i<isEditColor.length; i++){
            if(isEditColor[i])
                tv_days[i].setTextColor(Color.parseColor("#FF0000"));
            else
                tv_days[i].setTextColor(Color.parseColor("#8a000000"));
        }
    }
    private void AlertDialogSet(){
        LayoutInflater inflater=getLayoutInflater();
        final View dialogView= inflater.inflate(R.layout.layout_log, null);
        CheckBox cb_mon = (CheckBox) dialogView.findViewById(R.id.cb_mon);
        CheckBox cb_tue = (CheckBox) dialogView.findViewById(R.id.cb_tue);
        CheckBox cb_wed = (CheckBox) dialogView.findViewById(R.id.cb_wed);
        CheckBox cb_thur = (CheckBox) dialogView.findViewById(R.id.cb_thur);
        CheckBox cb_fri = (CheckBox) dialogView.findViewById(R.id.cb_fri);
        CheckBox cb_satur = (CheckBox) dialogView.findViewById(R.id.cb_satur);
        CheckBox cb_sun = (CheckBox) dialogView.findViewById(R.id.cb_sun);
        final CheckBox[] cbs = {cb_mon, cb_tue, cb_wed, cb_thur, cb_fri, cb_satur, cb_sun};
        if(isEdit){
            for(int i = 0; i<days.length; i++){
                if(days[i])
                    cbs[i].setChecked(true);
            }
        }
        //멤버의 세부내역 입력 Dialog 생성 및 보이기
        AlertDialog.Builder buider= new AlertDialog.Builder(this);
        buider.setTitle("알람 요일 선택");
        buider.setIcon(android.R.drawable.ic_menu_add);
        buider.setView(dialogView);
        buider.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < cbs.length; i++) {
                    days[i] = cbs[i].isChecked();
                    TextColorSet(days);
                }
                return;
            }
        });
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               return;
            }
        });

        //설정한 값으로 AlertDialog 객체 생성
        AlertDialog dialog=buider.create();
        dialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정
        dialog.show();
    }

    private void setLabel() {
        String date = String.valueOf(calendar.getTime());
        String[] splitdate = date.split(" ");
        //시 초
        date = splitdate[3].substring(0,5);
        tv_detail_time.setText(date);
    }

    private void ToastChat(String s){
        Toast.makeText(this,s+"을 정해주십시오.",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String detail_Time = "", detail_Title = null, detail_Days = "";
        //삭제 후 새로 생성
        if (isEdit)
            RealmDB.DeleteData(position, this);

        if (!tv_detail_time.getText().equals("시간을 선택하십시오."))
            detail_Time = tv_detail_time.getText().toString();
        else {
            ToastChat("시간");
            return true;
        }
        if (days[0] || days[1] || days[2] || days[3] || days[4] || days[5] || days[6]) {
            for (int i = 0; i < days.length; i++) {
                int day = days[i] ? 1 : 0;
                detail_Days += day + "-";
            }
            detail_Days = detail_Days.substring(0, (detail_Days.length() - 1));
            Log.e("#### Detail_Days", detail_Days);
        } else {
            ToastChat("요일");
            return false;
        }
        detail_Title = et_text.getText().toString();
        RealmDB.InsertItemData(detail_Time, detail_Title, detail_Days, this);

        Intent homeMove = new Intent(DetailActivity.this, MainActivity.class);
        homeMove.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(homeMove);
        return super.onOptionsItemSelected(item);
    }
}
