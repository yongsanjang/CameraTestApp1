package com.example.cameratestapp;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JavascriptBridge {

    @JavascriptInterface
    public void log(String msg) {
        Log.d("JavascriptBridge" , msg);
    }

//    @JavascriptInterface
//    public void setImage(String msg) {
//
//    }

}
