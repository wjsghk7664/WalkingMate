package com.example.walkingmate;

import android.graphics.drawable.Drawable;

public class RecyclerViewItem {
    private String mainTitle;
    private String userName;
    private String rDate;

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRDate(String rDate){ this.rDate = rDate; }

    public String getMainTitle() {
        return mainTitle;
    }

    public String getUserName() {
        return userName;
    }

    public String getRDate() { return rDate; }
}