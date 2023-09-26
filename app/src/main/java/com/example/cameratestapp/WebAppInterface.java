package com.example.cameratestapp;

import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.JsonIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;

public class WebAppInterface {
    private WebView webView;
    private WebAppInterfaceListener webAppInterfaceListener;
    private Map<String, String> runAsyncResults = new ConcurrentHashMap<>();

    public WebAppInterface(WebView wv, WebAppInterfaceListener webAppInterfaceListener) {
        webView = wv;
        this.webAppInterfaceListener = webAppInterfaceListener;
    }
    @JavascriptInterface
    public void runAsync(final String rand, final String funcName, final String jsonParams) {
        final WebAppInterface wai = this;
//        new android.os.Handler(Looper.getMainLooper()).post(() ->{
//            try {
//                final JSONObject params = new JSONObject(jsonParams);
//                String result = (String) wai.getClass().getMethod(funcName, JSONObject.class).invoke(wai, params);
//                wai.jsResolve(rand, true, result);
//            } catch (InvocationTargetException ite) { // exceptions inside the funcName function
//                wai.jsResolve(rand, false, ite.getCause().toString());
//            } catch (Exception e) {
//                wai.jsResolve(rand, false, e.toString());
//            }
//        });

//        new Thread() {
//            // runs the java function in a new thread
//            @Override public void run() {
                try {
                    final JSONObject params = new JSONObject(jsonParams);
                    String result = (String) wai.getClass().getMethod(funcName, JSONObject.class).invoke(wai, params);
                    wai.jsResolve(rand, true, result);
                } catch (InvocationTargetException ite) { // exceptions inside the funcName function
                    wai.jsResolve(rand, false, ite.getCause().toString());
                } catch (Exception e) {
                    wai.jsResolve(rand, false, e.toString());
                }
//            }
//        }.start();
    }
    private void jsResolve(String rand, boolean isSuccess, String result) { // notify that result is ready
        runAsyncResults.put(rand, result);
        final String url = "javascript:" + rand + ".callback(" + isSuccess + ")";
        Log.i("LOG_TAG", "calling js method with url " + url);
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(url);
            }
        });
    }
    @JavascriptInterface
    public String runAsyncResult(String rand) { // returns the result from runAsync to JS
        String result = runAsyncResults.get(rand);
        runAsyncResults.remove(rand);
        return result;
    }
    public String imgDelete(JSONObject src) throws JSONException {
        return webAppInterfaceListener.imgDelete(src.getString("filePath"));
    }

    public String fileUpload(JSONObject src) throws JSONException {
        return webAppInterfaceListener.fileUpload(src.getString("zipFileName"));
    }

    interface WebAppInterfaceListener {
        public String imgDelete(String src);
        public String fileUpload(String zipFileName);
    }
}
