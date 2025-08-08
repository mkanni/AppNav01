package com.franco.epos.appnav01;

public class Item {


    private String fnType, itemID, itemLU, descr, price, inQty, stock, itemCode, sDate, eDate, staff, status, supCode, midasCode;
    private Integer count;
    boolean isSelected;

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

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


}


