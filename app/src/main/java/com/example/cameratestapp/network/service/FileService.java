package com.example.cameratestapp.network.service;

import com.example.cameratestapp.network.constants.APIConstants;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileService {

    @Multipart
    @POST(APIConstants.URL.FILE_UPLOAD)
    Observable<String> sendImage(@Part MultipartBody.Part imageFile);
}
