package com.example.cameratestapp;

import android.graphics.drawable.Drawable;

public class RecyclerItem {
    private Drawable iconDrawable ;
    private String titleStr ;
    private String descStr ;

    public String getImageFullPath() {
        return imageFullPath;
    }

    public RecyclerItem setImageFullPath(String imageFullPath) {
        this.imageFullPath = imageFullPath;
        return this;
    }

    private String imageFullPath ;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setDesc(String desc) {
        descStr = desc ;
    }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getDesc() {
        return this.descStr ;
    }
}
