package com.example.cameratestapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cameratestapp.network.UploadProgressListener;

import org.w3c.dom.Text;


public class ProgressBarDialog extends Dialog {

    private String title = null;
    private String message = null;
    private TextView title_Tv;
    private TextView message_tv;
    private TextView txt_percent;
    private ProgressBar progressBar;

    private final UploadProgressListener listener = (bytesRead, contentLength, done) -> {
        if (contentLength != 0) {
            if (progressBar.getMax() == 0) {
                progressBar.setProgress(0);
                progressBar.setMax(100);
            }
            double read = (double) bytesRead;
            double contentLen = (double) contentLength;
            int value = (int) (read / contentLen * 100);
            progressBar.setProgress(value);
            new Handler(Looper.getMainLooper()).post(() -> txt_percent.setText(String.format("%d%%", value)));
            Log.d(ProgressBar.class.getSimpleName(), "bytesRead " + bytesRead + " contentLength : " + contentLength);
            if (done) progressBar.setProgress(100);
        }
    };

    public ProgressBarDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public ProgressBarDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected ProgressBarDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.dialog_progressbar);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = DialogProgressbarBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(R.layout.dialog_progressbar);
        setCancelable(false);
        title_Tv = findViewById(R.id.title_tv);
        message_tv = findViewById(R.id.message_tv);
        progressBar = findViewById(R.id.progressBar);
        txt_percent = findViewById(R.id.txt_percent);

        if (title != null) {
            title_Tv.setVisibility(View.VISIBLE);
            title_Tv.setText(title);
        } else {
            title_Tv.setVisibility(View.GONE);
        }

        if (message != null) {
            message_tv.setVisibility(View.VISIBLE);
            message_tv.setText(message);
        } else {
            message_tv.setVisibility(View.GONE);
        }

        WindowManager.LayoutParams params = getWindow().getAttributes();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        params.width = (int) (size.x * 0.9);
        params.height = (int) (size.y * 0.9);
        getWindow().setAttributes(params);
    }

    public ProgressBarDialog setTitle(@Nullable String title) {
        this.title = title;
        if (title != null) {
            title_Tv.setVisibility(View.VISIBLE);
            title_Tv.setText(title);
        } else {
            title_Tv.setVisibility(View.GONE);
        }
        return this;
    }

    public ProgressBarDialog setMessage(@Nullable String message) {
        this.message = message;
        if (message != null) {
            message_tv.setVisibility(View.VISIBLE);
            message_tv.setText(message);
        } else {
            message_tv.setVisibility(View.GONE);
        }
        return this;
    }

//    public DownloadProgressListener getListener(){
//        return listener;
//    }

    public UploadProgressListener getListener() {
        return listener;
    }
}
