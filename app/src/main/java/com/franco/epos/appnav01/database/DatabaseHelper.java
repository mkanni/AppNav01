package com.franco.epos.appnav01.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


import com.franco.epos.appnav01.database.model.ItemUploadTB;
import com.franco.epos.appnav01.database.model.OPListTB;
import com.franco.epos.appnav01.database.model.PListTB;
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
        db.execSQL(OPListTB.CREATE_TABLE);
        db.execSQL(PListTB.CREATE_TABLE);
        db.execSQL(ItemUploadTB.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed

        db.execSQL("DROP TABLE IF EXISTS " + SettingsTB.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + OPListTB.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PListTB.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ItemUploadTB.TABLE_NAME);

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


    //Methods for Open Purchase order ListTB

    public long insertOPListTB(OPListTB oPListTB) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(OPListTB.COLUMN_PO_CODE, oPListTB.getPo_code());
        values.put(OPListTB.COLUMN_PO_DESC, oPListTB.getPo_desc());
        values.put(OPListTB.COLUMN_ITEM_LOOKUP, oPListTB.getItem_lookup());
        values.put(OPListTB.COLUMN_QTY, oPListTB.getQty());
        values.put(OPListTB.COLUMN_TYPE, oPListTB.getType());
        values.put(OPListTB.COLUMN_VENDOR_CODE, oPListTB.getVendor_code());
        values.put(OPListTB.COLUMN_ITEM_DESCR, oPListTB.getItem_descr());
        values.put(OPListTB.COLUMN_MIDAS_CODE, oPListTB.getMidas_code());
        values.put(OPListTB.COLUMN_RESTOCK, oPListTB.getRestock());

        // insert row
        long id = db.insert(OPListTB.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public OPListTB getOPListTB(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(OPListTB.TABLE_NAME,
                new String[]{OPListTB.COLUMN_ID, OPListTB.COLUMN_PO_CODE, OPListTB.COLUMN_PO_DESC, OPListTB.COLUMN_ITEM_LOOKUP, OPListTB.COLUMN_QTY, OPListTB.COLUMN_TYPE, OPListTB.COLUMN_VENDOR_CODE, OPListTB.COLUMN_ITEM_DESCR, OPListTB.COLUMN_MIDAS_CODE, OPListTB.COLUMN_RESTOCK},
                OPListTB.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        /*if (cursor != null)
            cursor.moveToFirst();*/

        OPListTB oPListTB = null;
        if( cursor != null && cursor.moveToFirst() ) {
            // prepare settings object
            oPListTB = new OPListTB(
                    cursor.getInt(cursor.getColumnIndex(OPListTB.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_PO_CODE)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_PO_DESC)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_ITEM_LOOKUP)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_QTY)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_VENDOR_CODE)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_ITEM_DESCR)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_MIDAS_CODE)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_RESTOCK)));

        }

        // close the db connection
        cursor.close();

        return oPListTB;
    }

    public OPListTB getOPListTBByIL(String itemLU) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(OPListTB.TABLE_NAME,
                new String[]{OPListTB.COLUMN_ID, OPListTB.COLUMN_PO_CODE, OPListTB.COLUMN_PO_DESC, OPListTB.COLUMN_ITEM_LOOKUP, OPListTB.COLUMN_QTY, OPListTB.COLUMN_TYPE, OPListTB.COLUMN_VENDOR_CODE, OPListTB.COLUMN_ITEM_DESCR, OPListTB.COLUMN_MIDAS_CODE, OPListTB.COLUMN_RESTOCK},
                OPListTB.COLUMN_ITEM_LOOKUP + "=?",
                new String[]{String.valueOf(itemLU)}, null, null, null, null);

        /*if (cursor != null)
            cursor.moveToFirst();*/

        OPListTB oPListTB = null;
        if( cursor != null && cursor.moveToFirst() ) {
            // prepare settings object
            oPListTB = new OPListTB(
                    cursor.getInt(cursor.getColumnIndex(OPListTB.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_PO_CODE)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_PO_DESC)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_ITEM_LOOKUP)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_QTY)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_VENDOR_CODE)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_ITEM_DESCR)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_MIDAS_CODE)),
                    cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_RESTOCK)));
        }

        // close the db connection
        cursor.close();

        return oPListTB;
    }

    public List<OPListTB> getAllOPListTB() {
        List<OPListTB> oPListTB = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + OPListTB.TABLE_NAME + " ORDER BY " +
                OPListTB.COLUMN_ID + " DESC";



        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                OPListTB oPListTBb = new OPListTB();
                oPListTBb.setId(cursor.getInt(cursor.getColumnIndex(OPListTB.COLUMN_ID)));
                oPListTBb.setPo_code(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_PO_CODE)));
                oPListTBb.setPo_desc(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_PO_DESC)));
                oPListTBb.setItem_lookup(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_ITEM_LOOKUP)));
                oPListTBb.setQty(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_QTY)));
                oPListTBb.setType(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_TYPE)));
                oPListTBb.setVendor_code(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_VENDOR_CODE)));
                oPListTBb.setItem_descr(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_ITEM_DESCR)));
                oPListTBb.setMidas_code(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_MIDAS_CODE)));
                oPListTBb.setRestock(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_RESTOCK)));

                oPListTB.add(oPListTBb);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return oPListTB;
    }

    public List<OPListTB> getAllOPListTBPOC(String itemPOCode) {
        List<OPListTB> oPListTB = new ArrayList<>();

        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(OPListTB.TABLE_NAME,
                new String[]{OPListTB.COLUMN_ID, OPListTB.COLUMN_PO_CODE, OPListTB.COLUMN_PO_DESC, OPListTB.COLUMN_ITEM_LOOKUP, OPListTB.COLUMN_QTY, OPListTB.COLUMN_TYPE, OPListTB.COLUMN_VENDOR_CODE, OPListTB.COLUMN_ITEM_DESCR, OPListTB.COLUMN_MIDAS_CODE, OPListTB.COLUMN_RESTOCK},
                OPListTB.COLUMN_PO_CODE + "=?",
                new String[]{String.valueOf(itemPOCode)}, null, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                OPListTB oPListTBb = new OPListTB();
                oPListTBb.setId(cursor.getInt(cursor.getColumnIndex(OPListTB.COLUMN_ID)));
                oPListTBb.setPo_code(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_PO_CODE)));
                oPListTBb.setPo_desc(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_PO_DESC)));
                oPListTBb.setItem_lookup(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_ITEM_LOOKUP)));
                oPListTBb.setQty(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_QTY)));
                oPListTBb.setType(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_TYPE)));
                oPListTBb.setVendor_code(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_VENDOR_CODE)));
                oPListTBb.setItem_descr(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_ITEM_DESCR)));
                oPListTBb.setMidas_code(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_MIDAS_CODE)));
                oPListTBb.setRestock(cursor.getString(cursor.getColumnIndex(OPListTB.COLUMN_RESTOCK)));

                oPListTB.add(oPListTBb);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return oPListTB;
    }

    public int getOPListTBCount() {
        String countQuery = "SELECT  * FROM " + OPListTB.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }
    public int getOPListTBCountByITL(String itemLU) {
        //String countQuery = "SELECT  * FROM " + OPListTB.TABLE_NAME + " WHERE " + OPListTB.COLUMN_ITEM_LOOKUP + "=" + itemLU;
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.query(OPListTB.TABLE_NAME,
                new String[]{OPListTB.COLUMN_ID},
                OPListTB.COLUMN_ITEM_LOOKUP + "=?",
                new String[]{String.valueOf(itemLU)}, null, null, null, null);

        //Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateOPListTB(OPListTB oPListTB) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(OPListTB.COLUMN_ITEM_LOOKUP, oPListTB.getItem_lookup());
        values.put(OPListTB.COLUMN_QTY, oPListTB.getQty());
        values.put(OPListTB.COLUMN_TYPE, oPListTB.getType());
        values.put(OPListTB.COLUMN_VENDOR_CODE, oPListTB.getVendor_code());
        values.put(OPListTB.COLUMN_ITEM_DESCR, oPListTB.getItem_descr());
        values.put(OPListTB.COLUMN_MIDAS_CODE, oPListTB.getMidas_code());
        values.put(OPListTB.COLUMN_RESTOCK, oPListTB.getRestock());

        // updating row
        return db.update(OPListTB.TABLE_NAME, values, OPListTB.COLUMN_ITEM_LOOKUP + " = ?",
                new String[]{String.valueOf(oPListTB.getItem_lookup())});
    }

    public void deleteOPListTB(OPListTB oPListTB) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(OPListTB.TABLE_NAME, OPListTB.COLUMN_ITEM_LOOKUP + " = ?",
                new String[]{String.valueOf(oPListTB.getItem_lookup())});
        db.close();
    }

    public String emptyOPListTB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ OPListTB.TABLE_NAME);
        db.close();
        return "Success";
    }


    //Methods for Purchase ListTB

    public long insertPListTB(PListTB pListTB) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(PListTB.COLUMN_ITEM_LOOKUP, pListTB.getItem_lookup());
        values.put(PListTB.COLUMN_BAR_CODE, pListTB.getBar_code());
        values.put(PListTB.COLUMN_ITEM_DESCR, pListTB.getItem_descr());
        values.put(PListTB.COLUMN_QTY, pListTB.getQty());
        values.put(PListTB.COLUMN_PRICE, pListTB.getPrice());


        // insert row
        long id = db.insert(PListTB.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public PListTB getPListTB(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(PListTB.TABLE_NAME,
                new String[]{PListTB.COLUMN_ID, PListTB.COLUMN_ITEM_LOOKUP, PListTB.COLUMN_BAR_CODE, PListTB.COLUMN_ITEM_DESCR, PListTB.COLUMN_QTY, PListTB.COLUMN_PRICE},
                PListTB.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        /*if (cursor != null)
            cursor.moveToFirst();*/

        PListTB pListTB = null;
        if( cursor != null && cursor.moveToFirst() ) {
            // prepare settings object
            pListTB = new PListTB(
                    cursor.getInt(cursor.getColumnIndex(PListTB.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_ITEM_LOOKUP)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_BAR_CODE)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_ITEM_DESCR)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_QTY)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_PRICE)));

        }

        // close the db connection
        cursor.close();

        return pListTB;
    }

    public PListTB getPListTBByIL(String itemLU) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(PListTB.TABLE_NAME,
                new String[]{PListTB.COLUMN_ID, PListTB.COLUMN_ITEM_LOOKUP, PListTB.COLUMN_BAR_CODE, PListTB.COLUMN_ITEM_DESCR, PListTB.COLUMN_QTY, PListTB.COLUMN_PRICE},
                PListTB.COLUMN_ITEM_LOOKUP + "=?",
                new String[]{String.valueOf(itemLU)}, null, null, null, null);

        /*if (cursor != null)
            cursor.moveToFirst();*/

        PListTB pListTB = null;
        if( cursor != null && cursor.moveToFirst() ) {
            // prepare settings object
            pListTB = new PListTB(
                    cursor.getInt(cursor.getColumnIndex(PListTB.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_ITEM_LOOKUP)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_BAR_CODE)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_ITEM_DESCR)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_QTY)),
                    cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_PRICE)));
        }

        // close the db connection
        cursor.close();

        return pListTB;
    }

    public List<PListTB> getAllPListTB() {
        List<PListTB> pListTB = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + PListTB.TABLE_NAME + " ORDER BY " +
                PListTB.COLUMN_ID + " DESC";



        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PListTB pListTBb = new PListTB();
                pListTBb.setId(cursor.getInt(cursor.getColumnIndex(PListTB.COLUMN_ID)));
                pListTBb.setItem_lookup(cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_ITEM_LOOKUP)));
                pListTBb.setBar_code(cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_BAR_CODE)));
                pListTBb.setItem_descr(cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_ITEM_DESCR)));
                pListTBb.setQty(cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_QTY)));
                pListTBb.setPrice(cursor.getString(cursor.getColumnIndex(PListTB.COLUMN_PRICE)));


                pListTB.add(pListTBb);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return pListTB;
    }



    public int getPListTBCount() {
        String countQuery = "SELECT  * FROM " + PListTB.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }
    public int getPListTBCountByITL(String itemLU) {
        //String countQuery = "SELECT  * FROM " + PListTB.TABLE_NAME + " WHERE " + PListTB.COLUMN_ITEM_LOOKUP + "=" + itemLU;
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.query(PListTB.TABLE_NAME,
                new String[]{PListTB.COLUMN_ID},
                PListTB.COLUMN_ITEM_LOOKUP + "=?",
                new String[]{String.valueOf(itemLU)}, null, null, null, null);

        //Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updatePListTB(PListTB pListTB) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PListTB.COLUMN_ITEM_LOOKUP, pListTB.getItem_lookup());
        values.put(PListTB.COLUMN_BAR_CODE, pListTB.getBar_code());
        values.put(PListTB.COLUMN_ITEM_DESCR, pListTB.getItem_descr());
        values.put(PListTB.COLUMN_QTY, pListTB.getQty());
        values.put(PListTB.COLUMN_PRICE, pListTB.getPrice());


        // updating row
        return db.update(PListTB.TABLE_NAME, values, PListTB.COLUMN_ITEM_LOOKUP + " = ?",
                new String[]{String.valueOf(pListTB.getItem_lookup())});
    }

    public void deletePListTB(PListTB pListTB) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PListTB.TABLE_NAME, PListTB.COLUMN_ITEM_LOOKUP + " = ?",
                new String[]{String.valueOf(pListTB.getItem_lookup())});
        db.close();
    }

    public String emptyPListTB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ PListTB.TABLE_NAME);
        db.close();
        return "Success";
    }


    //Methods for ItemUpload ItemUploadTb

    public long insertItemUploadTB(ItemUploadTB itemUploadTB) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(ItemUploadTB.COLUMN_ACTION, itemUploadTB.getAction());
        values.put(ItemUploadTB.COLUMN_ITEM_LOOKUP, itemUploadTB.getItem_lookup());
        values.put(ItemUploadTB.COLUMN_DESCR, itemUploadTB.getDescr());
        values.put(ItemUploadTB.COLUMN_BARCODE, itemUploadTB.getBarcode());
        values.put(ItemUploadTB.COLUMN_VAT, itemUploadTB.getVat());
        values.put(ItemUploadTB.COLUMN_PRICE, itemUploadTB.getPrice());
        values.put(ItemUploadTB.COLUMN_COST, itemUploadTB.getCost());
        values.put(ItemUploadTB.COLUMN_DEP_DESCR, itemUploadTB.getDep_descr());
        values.put(ItemUploadTB.COLUMN_DEP_GROUPS, itemUploadTB.getDep_groups());
        values.put(ItemUploadTB.COLUMN_DEP_AGE_CHECK, itemUploadTB.getDep_age_check());
        values.put(ItemUploadTB.COLUMN_DEP_COMMISSION, itemUploadTB.getDep_commission());
        values.put(ItemUploadTB.COLUMN_DEP_VAT, itemUploadTB.getDep_vat());
        values.put(ItemUploadTB.COLUMN_CAT_DESCR, itemUploadTB.getCat_descr());
        values.put(ItemUploadTB.COLUMN_CAT_VAT, itemUploadTB.getCat_vat());




        // insert row
        long id = db.insert(ItemUploadTB.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public List<ItemUploadTB> getAllItemUploadTB() {
        List<ItemUploadTB> itemUploadTB = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + ItemUploadTB.TABLE_NAME + " ORDER BY " +
                ItemUploadTB.COLUMN_ID + " DESC";



        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ItemUploadTB itemUploadTBb = new ItemUploadTB();
                itemUploadTBb.setId(cursor.getInt(cursor.getColumnIndex(ItemUploadTB.COLUMN_ID)));
                itemUploadTBb.setAction(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_ACTION)));
                itemUploadTBb.setItem_lookup(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_ITEM_LOOKUP)));
                itemUploadTBb.setDescr(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_DESCR)));
                itemUploadTBb.setBarcode(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_BARCODE)));
                itemUploadTBb.setVat(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_VAT)));
                itemUploadTBb.setPrice(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_PRICE)));
                itemUploadTBb.setCost(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_COST)));
                itemUploadTBb.setDep_descr(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_DEP_DESCR)));
                itemUploadTBb.setDep_groups(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_DEP_GROUPS)));
                itemUploadTBb.setDep_age_check(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_DEP_AGE_CHECK)));
                itemUploadTBb.setDep_commission(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_DEP_COMMISSION)));
                itemUploadTBb.setDep_vat(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_DEP_VAT)));
                itemUploadTBb.setCat_descr(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_CAT_DESCR)));
                itemUploadTBb.setCat_vat(cursor.getString(cursor.getColumnIndex(ItemUploadTB.COLUMN_CAT_VAT)));


                itemUploadTB.add(itemUploadTBb);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return itemUploadTB;
    }


    public void deleteItemUploadTBID(ItemUploadTB itemUploadTB) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ItemUploadTB.TABLE_NAME, ItemUploadTB.COLUMN_ID + " = ?",
                new String[]{String.valueOf(itemUploadTB.getId())});
        db.close();
    }
    public void deleteItemUploadTBIL(ItemUploadTB itemUploadTB) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ItemUploadTB.TABLE_NAME, ItemUploadTB.COLUMN_ITEM_LOOKUP + " = ?",
                new String[]{String.valueOf(itemUploadTB.getItem_lookup())});
        db.close();
    }

    public String emptyItemUploadTB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ ItemUploadTB.TABLE_NAME);
        db.close();
        return "Success";
    }



}
