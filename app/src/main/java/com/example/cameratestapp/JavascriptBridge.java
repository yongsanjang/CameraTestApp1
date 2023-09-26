package com.example.cameratestapp;

import android.util.Log;
import android.webkit.JavascriptInterface;

interface JavascriptBridge {

    @JavascriptInterface
    public void log(String msg);

    @JavascriptInterface
    public void imgDelete(String src ,Object obj) ;
}
