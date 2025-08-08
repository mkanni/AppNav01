package com.franco.epos.appnav01.database.model;

public class SettingsTB {

    public static final String TABLE_NAME = "settings_tbl";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TILL_TYPE = "till_type";
    public static final String COLUMN_IPADDRESS = "ipaddress";
    public static final String COLUMN_PORT = "port";

    private int id;
    private String till_type;
    private String ipaddress;
    private String port;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TILL_TYPE + " TEXT,"
                    + COLUMN_IPADDRESS + " TEXT,"
                    + COLUMN_PORT + " TEXT"
                    + ")";

    public SettingsTB() {
    }

    public SettingsTB(int id, String till_type, String ipaddress, String port) {
        this.id = id;
        this.till_type = till_type;
        this.ipaddress = ipaddress;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getTill_type() {
        return till_type;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public String getPort() {
        return port;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTill_type(String till_type) {
        this.till_type = till_type;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
