package com.franco.epos.appnav01.database.model;

public class ItemUploadTB {
    public static final String TABLE_NAME = "item_upload_tbl";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ACTION = "action";
    public static final String COLUMN_ITEM_LOOKUP = "item_lookup";
    public static final String COLUMN_DESCR = "descr";
    public static final String COLUMN_BARCODE = "barcode";
    public static final String COLUMN_VAT = "vat";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_COST = "cost";
    public static final String COLUMN_DEP_DESCR = "dep_descr";
    public static final String COLUMN_DEP_GROUPS = "dep_groups";
    public static final String COLUMN_DEP_AGE_CHECK = "dep_age_check";
    public static final String COLUMN_DEP_COMMISSION = "dep_commission";
    public static final String COLUMN_DEP_VAT = "dep_vat";
    public static final String COLUMN_CAT_DESCR = "cat_descr";
    public static final String COLUMN_CAT_VAT = "cat_vat";



    private int id;
    private String action;
    private String item_lookup;
    private String descr;
    private String barcode;
    private String vat;
    private String price;
    private String cost;
    private String dep_descr;
    private String dep_groups;
    private String dep_age_check;
    private String dep_commission;
    private String dep_vat;
    private String cat_descr;
    private String cat_vat;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_ACTION + " TEXT,"
                    + COLUMN_ITEM_LOOKUP + " TEXT,"
                    + COLUMN_DESCR + " TEXT,"
                    + COLUMN_BARCODE + " TEXT,"
                    + COLUMN_VAT + " TEXT,"
                    + COLUMN_PRICE + " TEXT,"
                    + COLUMN_COST + " TEXT,"
                    + COLUMN_DEP_DESCR + " TEXT,"
                    + COLUMN_DEP_GROUPS + " TEXT,"
                    + COLUMN_DEP_AGE_CHECK + " TEXT,"
                    + COLUMN_DEP_COMMISSION + " TEXT,"
                    + COLUMN_DEP_VAT + " TEXT,"
                    + COLUMN_CAT_DESCR + " TEXT,"
                    + COLUMN_CAT_VAT + " TEXT"
                    + ")";

    public ItemUploadTB() {
    }



    public ItemUploadTB(int id, String action, String item_lookup, String descr, String barcode, String vat, String price, String cost, String dep_descr, String dep_groups, String dep_age_check, String dep_commission, String dep_vat, String cat_descr, String cat_vat) {
        this.id = id;
        this.action = action;
        this.item_lookup = item_lookup;
        this.descr = descr;
        this.barcode = barcode;
        this.vat = vat;
        this.price = price;
        this.cost = cost;
        this.dep_descr = dep_descr;
        this.dep_groups = dep_groups;
        this.dep_age_check = dep_age_check;
        this.dep_commission = dep_commission;
        this.dep_vat = dep_vat;
        this.cat_descr = cat_descr;
        this.cat_vat = cat_vat;

    }



    public int getId() {
        return id;
    }
    public String getAction() { return action; }
    public String getItem_lookup() {
        return item_lookup;
    }
    public String getDescr() {
        return descr;
    }

    public String getBarcode() { return barcode; }
    public String getVat() { return vat; }
    public String getPrice() { return price; }
    public String getCost() { return cost; }
    public String getDep_descr() { return dep_descr; }
    public String getDep_groups() { return dep_groups; }
    public String getDep_age_check() { return dep_age_check; }
    public String getDep_commission() { return dep_commission; }
    public String getDep_vat() { return dep_vat; }
    public String getCat_descr() { return cat_descr; }
    public String getCat_vat() { return cat_vat; }

    public void setId(int id) {
        this.id = id;
    }

    public void setAction(String action) {
        this.action = action;
    }
    public void setItem_lookup(String item_lookup) {
        this.item_lookup = item_lookup;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }



    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public void setPrice(String price) {
        this.price = price;
    }
    public void setCost(String cost) {
        this.cost = cost;
    }

    public void setDep_descr(String dep_descr) {
        this.dep_descr = dep_descr;
    }
    public void setDep_groups(String dep_groups) {
        this.dep_groups = dep_groups;
    }

    public void setDep_age_check(String dep_age_check) {
        this.dep_age_check = dep_age_check;
    }

    public void setDep_commission(String dep_commission) {
        this.dep_commission = dep_commission;
    }
    public void setDep_vat(String dep_vat) {
        this.dep_vat = dep_vat;
    }

    public void setCat_descr(String cat_descr) {
        this.cat_descr = cat_descr;
    }
    public void setCat_vat(String cat_vat) {
        this.cat_vat = cat_vat;
    }
}
