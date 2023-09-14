package com.example.cameratestapp.network;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.example.cameratestapp.ProgressBarDialog;
import com.example.cameratestapp.network.service.FileService;

import java.io.File;
import java.lang.ref.WeakReference;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ApiRequest {

    private static String mBaseUrl;
    private static volatile ApiRequest instance;
    private static FileService fileService;
    protected ProgressBarDialog progressbar = null;
    private WeakReference<Context> contextWeakReference;

    public static ApiRequest get() {
        synchronized (ApiRequest.class) {
            if (instance == null) {
                instance = new ApiRequest();
            }
            return instance;
        }
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public Boolean createRetrofit(@Nullable String baseUrl) {
        mBaseUrl = baseUrl;
        if (!TextUtils.isEmpty(baseUrl)) {
            fileService = RetrofitModule.createRetrofit(baseUrl, FileService.class);
            return true;
        } else {
            return false;
        }
    }

    public void fileUpload(Observer<String> observer, Context context, File file) {
        fileUpload(observer, context, file, false);
    }

    public void fileUpload(Observer<String> observer, Context context, File file, boolean isProgressbarShow) {
        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);
        RequestBody requestFile = RequestBody.create(MediaType.parse(context.getContentResolver().getType(fileUri)), file);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), new UploadProgressRequestBody(requestFile, uploadProgressListener));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), new UploadProgressRequestBody(requestFile, isProgressbarShow ? showProgressBar(context).setMessage("파일 업로드").getListener(): null));
        fileService.sendImage(body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(msg ->{
                    Log.d("fileUpload", msg);
                })
                .doOnComplete(() -> {
                    dismissProgressBar();
//                    Log.d("fileUpload", "Complete");
                })
                .doOnError(e -> {
                    dismissProgressBar();
//                    if (listener == null) progressHelper.dismissProgressBar();
                    Log.e("fileUpload", e.getMessage());
                })
                .subscribe(observer);
    }

    private void createProgressBar(Context context) {
        dismissProgressBar();
        contextWeakReference =  new WeakReference(context);
        progressbar = new ProgressBarDialog(contextWeakReference.get());
    }

    private ProgressBarDialog showProgressBar(Context context){
        createProgressBar(context);
        if(progressbar != null && !progressbar.isShowing()){
            progressbar.show();
        }
        return progressbar;
    }

    private void dismissProgressBar(){
        if(progressbar != null && progressbar.isShowing()){
            progressbar.dismiss();
        }
    }
}
