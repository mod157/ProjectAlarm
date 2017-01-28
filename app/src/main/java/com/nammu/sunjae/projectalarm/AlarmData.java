package com.nammu.sunjae.projectalarm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by SunJae on 2017-01-24.
 */

public class AlarmData extends RealmObject {

    private String time;
    private String days;
    private String title;

    public void setTime(String s){
        time = s;
    }

    public void setDays(String s){
        days = s;
    }

    public void setTitle(String s){
        title = s;
    }

    public String getTime(){
        return time;
    }

    public String getDays(){
        return days;
    }

    public String getTitle(){
        return title;
    }

}
