package com.zjw.appmethodtime;

import com.google.gson.annotations.SerializedName;

public class SearchShopModel extends SearchCoreModel {
    @SerializedName(value = "title")
    public String shop;

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    @Override
    public String toString() {
        return "SearchShopModel{" +
                "shop='" + shop + '\'' +
                '}' + super.toString();
    }
}
