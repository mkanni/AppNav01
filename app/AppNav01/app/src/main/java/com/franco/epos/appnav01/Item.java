package com.franco.epos.appnav01;

public class Item {


    private String fnType, itemID, itemLU, descr, price, inQty;
    private Integer count;

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


}


