package com.zjw.appmethodtime;

import com.google.gson.annotations.SerializedName;

public class SearchCoreModel extends BaseModel {
    @SerializedName(value = "title")
    public String base;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    @Override
    public String toString() {
        return "SearchCoreModel{" +
                "base='" + base + '\'' +
                '}'+super.toString();
    }
}
