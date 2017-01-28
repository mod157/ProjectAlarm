package com.nammu.sunjae.projectalarm;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by SunJae on 2017-01-26.
 */

public class RealmDB {
    public static Realm RealmInit(Context context){
        Realm realm = null;
        realm.init(context);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();
        return realm;
    }

    public static void InsertItemData(String time, String title, String days, Context context){
        Realm realm = RealmInit(context);
        if(realm.isInTransaction())
            realm.commitTransaction();
        realm.beginTransaction();
        AlarmData data = realm.createObject(AlarmData.class);
        data.setTime(time);
        data.setTitle(title);
        data.setDays(days);
        realm.commitTransaction();
    }

    public static void DeleteData(int position, Context context){
        Realm realm = RealmInit(context);
        if(realm.isInTransaction())
            realm.commitTransaction();
        realm.beginTransaction();
        RealmResults<AlarmData> list = realm.where(AlarmData.class).findAllSorted("time");
        AlarmData deleteData = list.get(position);
        deleteData.deleteFromRealm();
        realm.commitTransaction();
    }
}
