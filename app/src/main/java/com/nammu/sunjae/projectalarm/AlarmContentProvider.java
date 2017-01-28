package com.nammu.sunjae.projectalarm;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by SunJae on 2017-01-27.
 */

public class AlarmContentProvider extends ContentProvider {
    public Realm realm;
    String [] sColumns = new String [] { "time", "title", "days"};
    Context context;
    @Override
    public boolean onCreate() {
        context= getContext();
        realm.init(getContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();
        return (realm == null)? false : true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        realm = RealmDB.RealmInit(context);
        RealmResults<AlarmData> results = realm.where(AlarmData.class).findAllSorted("time");
        MatrixCursor matrixCursor = new MatrixCursor(sColumns);
        for (AlarmData item : results) {
            Object[] rowData = new Object[]{item.getTime(),item.getTitle(), item.getDays()};
            matrixCursor.addRow(rowData);
        }
        return matrixCursor;
        //조회
        /*
        Cursor c =  contentResolver.query(Uri.parse(URL), null, null, null, null);
        List<String> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(c.getString(c.getColumnIndex("time")));
        }
        result.setText(list.toString());*/
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*추가
        * ContentValues values1 = new ContentValues();
                values1.put("time", "09:00");
                values1.put("title", "딴데서 넣음");
                values1.put("days", "1-1-1-1-1-1-1");
                contentResolver.insert(Uri.parse(URL), values1);*/

        realm = RealmDB.RealmInit(context);
        realm.beginTransaction ();
        AlarmData item = realm.createObject(AlarmData.class);
        item.setTime (values.getAsString(sColumns[0]));
        item.setTitle (values.getAsString (sColumns [1]));
        item.setDays (values.getAsString (sColumns [2]));
        realm.commitTransaction ();
        return Uri.withAppendedPath (uri, item.getTime()+", " + item.getTitle() +", " + item.getDays() );
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        realm = RealmDB.RealmInit(context);
        realm.beginTransaction();
        RealmResults<AlarmData> list = realm.where(AlarmData.class).findAllSorted("time");
        AlarmData deleteData = list.get(Integer.parseInt(selection));
        deleteData.deleteFromRealm();
        realm.commitTransaction();
        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        delete(uri, selection, selectionArgs);
        insert(uri, values);
        return 0;
    }
}
