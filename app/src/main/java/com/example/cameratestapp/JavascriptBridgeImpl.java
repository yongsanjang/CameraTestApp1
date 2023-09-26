package com.example.cameratestapp;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JavascriptBridgeImpl {

    private JavascriptBridge javascriptBridge;
    public JavascriptBridgeImpl(JavascriptBridge javascriptBridge) {
        this.javascriptBridge = javascriptBridge;
    }
    @JavascriptInterface
    public void log(String msg) {
        Log.d("JavascriptBridge" , msg);
        javascriptBridge.log(msg);
    }

    @JavascriptInterface
    public void imgDelete(String src ,Object obj){
        Log.d(getClass().getSimpleName(),obj+"");
        javascriptBridge.imgDelete(src, obj);
    }
}
