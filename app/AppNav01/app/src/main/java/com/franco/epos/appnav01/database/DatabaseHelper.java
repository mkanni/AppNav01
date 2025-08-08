package com.franco.epos.appnav01.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


import com.franco.epos.appnav01.database.model.SettingsTB;

/**
 * Created by ravi on 15/03/18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ePos_Master";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create settings table

        db.execSQL(SettingsTB.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed

        db.execSQL("DROP TABLE IF EXISTS " + SettingsTB.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }


    //Methods for settings

    public long insertSettingsTB(SettingsTB settingsTB) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(SettingsTB.COLUMN_TILL_TYPE, settingsTB.getTill_type());
        values.put(SettingsTB.COLUMN_IPADDRESS, settingsTB.getIpaddress());
        values.put(SettingsTB.COLUMN_PORT, settingsTB.getPort());

        // insert row
        long id = db.insert(SettingsTB.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public SettingsTB getSettingsTB(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(SettingsTB.TABLE_NAME,
                new String[]{SettingsTB.COLUMN_ID, SettingsTB.COLUMN_TILL_TYPE, SettingsTB.COLUMN_IPADDRESS, SettingsTB.COLUMN_PORT},
                SettingsTB.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        /*if (cursor != null)
            cursor.moveToFirst();*/

        SettingsTB settingsTB = null;
        if( cursor != null && cursor.moveToFirst() ) {
            // prepare settings object
            settingsTB = new SettingsTB(
                    cursor.getInt(cursor.getColumnIndex(SettingsTB.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(SettingsTB.COLUMN_TILL_TYPE)),
                    cursor.getString(cursor.getColumnIndex(SettingsTB.COLUMN_IPADDRESS)),
                    cursor.getString(cursor.getColumnIndex(SettingsTB.COLUMN_PORT)));
        }

        // close the db connection
        cursor.close();

        return settingsTB;
    }

    public SettingsTB getSetTBByType(String tillType) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(SettingsTB.TABLE_NAME,
                new String[]{SettingsTB.COLUMN_ID, SettingsTB.COLUMN_TILL_TYPE, SettingsTB.COLUMN_IPADDRESS, SettingsTB.COLUMN_PORT},
                SettingsTB.COLUMN_TILL_TYPE + "=?",
                new String[]{String.valueOf(tillType)}, null, null, null, null);

        /*if (cursor != null)
            cursor.moveToFirst();*/
        SettingsTB settingsTB = null;
        if( cursor != null && cursor.moveToFirst() ){
            // prepare settings object
            settingsTB = new SettingsTB(
                    cursor.getInt(cursor.getColumnIndex(SettingsTB.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(SettingsTB.COLUMN_TILL_TYPE)),
                    cursor.getString(cursor.getColumnIndex(SettingsTB.COLUMN_IPADDRESS)),
                    cursor.getString(cursor.getColumnIndex(SettingsTB.COLUMN_PORT)));


        }

        // close the db connection
        cursor.close();

        return settingsTB;
    }

    public List<SettingsTB> getAllSettingsTB() {
        List<SettingsTB> settingsTB = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + SettingsTB.TABLE_NAME + " ORDER BY " +
                SettingsTB.COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SettingsTB settingsTBb = new SettingsTB();
                settingsTBb.setId(cursor.getInt(cursor.getColumnIndex(SettingsTB.COLUMN_ID)));
                settingsTBb.setTill_type(cursor.getString(cursor.getColumnIndex(SettingsTB.COLUMN_TILL_TYPE)));
                settingsTBb.setIpaddress(cursor.getString(cursor.getColumnIndex(SettingsTB.COLUMN_IPADDRESS)));
                settingsTBb.setPort(cursor.getString(cursor.getColumnIndex(SettingsTB.COLUMN_PORT)));
                settingsTB.add(settingsTBb);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return settingsTB;
    }

    public int getSettingsTBCount() {
        String countQuery = "SELECT  * FROM " + SettingsTB.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateSettingsTB(SettingsTB settingsTB) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SettingsTB.COLUMN_TILL_TYPE, settingsTB.getTill_type());
        values.put(SettingsTB.COLUMN_IPADDRESS, settingsTB.getIpaddress());
        values.put(SettingsTB.COLUMN_PORT, settingsTB.getPort());

        // updating row
        return db.update(SettingsTB.TABLE_NAME, values, SettingsTB.COLUMN_TILL_TYPE + " = ?",
                new String[]{String.valueOf(settingsTB.getTill_type())});
    }

    public void deleteSettingsTB(SettingsTB settingsTB) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SettingsTB.TABLE_NAME, SettingsTB.COLUMN_ID + " = ?",
                new String[]{String.valueOf(settingsTB.getId())});
        db.close();
    }
}
