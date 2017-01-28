package com.nammu.sunjae.projectalarm;

/**
 * Created by SunJae on 2017-01-24.
 */

public class AlarmItem {
    private String time;
    private boolean[] days = new boolean[7];

    public void setTime(String time){
        this.time = time;
    }

    public void setDays(boolean[] days){
        this.days = days;
    }

    public String getTime(){
        return time;
    }

    public boolean[] getDays(){
        return days;
    }
}
