package com.franco.epos.appnav01.database.model;

public class OPListTB {

    public static final String TABLE_NAME = "purchase_order_tbl";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PO_CODE = "po_code";
    public static final String COLUMN_PO_DESC = "po_desc";
    public static final String COLUMN_ITEM_LOOKUP = "item_lookup";
    public static final String COLUMN_QTY = "qty";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_VENDOR_CODE = "vendor_code";
    public static final String COLUMN_ITEM_DESCR = "item_descr";
    public static final String COLUMN_MIDAS_CODE = "midas_code";
    public static final String COLUMN_RESTOCK = "restock";



    private int id;
    private String po_code;
    private String po_desc;
    private String item_lookup;
    private String qty;
    private String type;
    private String vendor_code;
    private String item_descr;
    private String midas_code;
    private String restock;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_PO_CODE + " TEXT,"
                    + COLUMN_PO_DESC + " TEXT,"
                    + COLUMN_ITEM_LOOKUP + " TEXT,"
                    + COLUMN_QTY + " TEXT,"
                    + COLUMN_TYPE + " TEXT,"
                    + COLUMN_VENDOR_CODE + " TEXT,"
                    + COLUMN_ITEM_DESCR + " TEXT,"
                    + COLUMN_MIDAS_CODE + " TEXT,"
                    + COLUMN_RESTOCK + " TEXT"
                    + ")";

    public OPListTB() {
    }

    public OPListTB(int id, String po_code, String po_desc, String item_lookup, String qty, String type, String vendor_code, String item_descr, String midas_code, String restock) {
        this.id = id;
        this.po_code = po_code;
        this.po_desc = po_desc;
        this.item_lookup = item_lookup;
        this.qty = qty;
        this.type = type;
        this.vendor_code = vendor_code;
        this.item_descr = item_descr;
        this.midas_code = midas_code;
        this.restock = restock;
    }

    public int getId() {
        return id;
    }

    public String getPo_code() { return po_code; }

    public String getPo_desc() {
        return po_desc;
    }

    public String getItem_lookup() {
        return item_lookup;
    }
    public String getQty() { return qty; }
    public String getType() { return type; }
    public String getVendor_code() { return vendor_code; }
    public String getItem_descr() { return item_descr; }
    public String getMidas_code() { return midas_code; }
    public String getRestock() { return restock; }

    public void setId(int id) {
        this.id = id;
    }

    public void setPo_code(String po_code) {
        this.po_code = po_code;
    }

    public void setPo_desc(String po_desc) {
        this.po_desc = po_desc;
    }

    public void setItem_lookup(String item_lookup) {
        this.item_lookup = item_lookup;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVendor_code(String vendor_code) {
        this.vendor_code = vendor_code;
    }
    public void setItem_descr(String item_descr) {
        this.item_descr = item_descr;
    }

    public void setMidas_code(String midas_code) {
        this.midas_code = midas_code;
    }
    public void setRestock(String restock) {
        this.restock = restock;
    }
}
