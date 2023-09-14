package com.example.cameratestapp.network;

public interface UploadProgressListener {
    void onRequestProgress(long bytesRead, long contentLength, boolean done);
}
