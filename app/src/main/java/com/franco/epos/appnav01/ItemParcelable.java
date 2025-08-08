package com.franco.epos.appnav01;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemParcelable implements Parcelable {


    private String fnType, itemID, itemLU, descr, price, inQty, itemCode, sDate, eDate, staff, status, supCode, midasCode;
    private Integer count;

    // Constructor
    public ItemParcelable(String fnType, String itemID, String itemLU, String descr, String price, String inQty, String itemCode, String sDate, String eDate, String staff, String status, String supCode, String midasCode){
        this.fnType = fnType;
        this.itemID = itemID;
        this.itemLU = itemLU;
        this.descr = descr;
        this.price = price;
        this.inQty = inQty;
        this.itemCode = itemCode;
        this.sDate = sDate;
        this.eDate = eDate;
        this.staff = staff;
        this.status = status;
        this.supCode = supCode;
        this.midasCode = midasCode;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getFnType() {
        return fnType;
    }

    public void setFnType(String fnType) {
        this.fnType = fnType;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemLU() {
        return itemLU;
    }

    public void setItemLU(String itemLU) {
        this.itemLU = itemLU;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getInQty() {
        return inQty;
    }

    public void setInQty(String inQty) {
        this.inQty = inQty;
    }
    //itemCode, sDate, eDate, staff, status
    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getSDate() {
        return sDate;
    }

    public void setSDate(String sDate) {
        this.sDate = sDate;
    }

    public String getEDate() {
        return eDate;
    }

    public void setEDate(String eDate) {
        this.eDate = eDate;
    }

    public String getStaff() {
        return staff;
    }

    public void setStaff(String staff) {
        this.staff = staff;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSupCode() { return supCode; }

    public void setSupCode(String supCode) {
        this.supCode = supCode;
    }

    public String getMidasCode() { return midasCode; }

    public void setMidasCode(String midasCode) {
        this.midasCode = midasCode;
    }


    @Override
    public int describeContents() {
        return 0;
    }



    // Parcelling part
    public ItemParcelable(Parcel in){
        String[] data = new String[13];


        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.fnType = data[0];
        this.itemID = data[1];
        this.itemLU = data[2];
        this.descr = data[3];
        this.price = data[4];
        this.inQty = data[5];
        this.itemCode = data[6];
        this.sDate = data[7];
        this.eDate = data[8];
        this.staff = data[9];
        this.status = data[10];
        this.supCode = data[11];
        this.midasCode = data[12];

    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.fnType,
                this.itemID,
                this.itemLU,
                this.descr,
                this.price,
                this.inQty,
                this.itemCode,
                this.sDate,
                this.eDate,
                this.staff,
                this.status,
                this.supCode,
                this.midasCode});
    }
    public static final Creator CREATOR = new Creator() {
        public ItemParcelable createFromParcel(Parcel in) {
            return new ItemParcelable(in);
        }

        public ItemParcelable[] newArray(int size) {
            return new ItemParcelable[size];
        }
    };
}


