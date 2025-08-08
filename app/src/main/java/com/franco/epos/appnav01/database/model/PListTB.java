package com.franco.epos.appnav01.database.model;

public class PListTB {

    public static final String TABLE_NAME = "purchase_tbl";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ITEM_LOOKUP = "item_lookup";
    public static final String COLUMN_BAR_CODE = "bar_code";
    public static final String COLUMN_ITEM_DESCR = "item_descr";
    public static final String COLUMN_QTY = "qty";
    public static final String COLUMN_PRICE = "price";



    private int id;
    private String item_lookup;
    private String bar_code;
    private String item_descr;
    private String qty;
    private String price;



    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_ITEM_LOOKUP + " TEXT,"
                    + COLUMN_BAR_CODE + " TEXT,"
                    + COLUMN_ITEM_DESCR + " TEXT,"
                    + COLUMN_QTY + " TEXT,"
                    + COLUMN_PRICE + " TEXT"
                    + ")";

    public PListTB() {
    }

    public PListTB(int id, String item_lookup, String bar_code,  String item_descr, String qty, String price) {
        this.id = id;
        this.item_lookup = item_lookup;
        this.bar_code = bar_code;
        this.item_descr = item_descr;
        this.qty = qty;
        this.price = price;

    }

    public int getId() {
        return id;
    }
    public String getItem_lookup() {
        return item_lookup;
    }
    public String getBar_code() { return bar_code; }
    public String getItem_descr() { return item_descr; }
    public String getQty() { return qty; }
    public String getPrice() { return price; }


    public void setId(int id) {
        this.id = id;
    }
    public void setItem_lookup(String item_lookup) {
        this.item_lookup = item_lookup;
    }
    public void setBar_code(String bar_code) {
        this.bar_code = bar_code;
    }
    public void setItem_descr(String item_descr) {
        this.item_descr = item_descr;
    }
    public void setQty(String qty) {
        this.qty = qty;
    }
    public void setPrice(String price) {
        this.price = price;
    }




}
