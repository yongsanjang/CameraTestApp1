package com.example.cameratestapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cameratestapp.network.ApiRequest;
import com.example.cameratestapp.network.RetrofitModule;
import com.example.cameratestapp.network.UploadProgressListener;
import com.example.cameratestapp.network.UploadProgressRequestBody;
import com.example.cameratestapp.network.service.FileService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import io.reactivex.observers.DisposableObserver;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends Activity implements JavascriptBridge {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int maxFileSize = 50;
    private static final String COMPRESS_FOLDER_NAME = "Compress";

    private RecyclerImageTextAdapter adapter;
    private String currentPhotoPath;
    private WebView web_view;
    private DisposableObserver<String> observer;
    private String uploadZipFilePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ApiRequest.get().createRetrofit("http://192.168.180.107:8080/");
        init();
    }

    private void fileUpload(View view) {
        nativeFileUpload( "imgZip_0001.zip");
//        String pathname = "/storage/emulated/0/Android/data/com.example.cameratestapp/files/Pictures/20230913_174719_7979256138469879015.jpg";
//        String pathname = "/storage/emulated/0/Android/data/com.example.cameratestapp/files/20230912_144805.zip";

//        Uri fileUri = Uri.fromFile(new File(pathname));
//        File file = FileUtils.getFile(this, fileUri);
//        File file = new File(pathname);
//        Uri fileUri = FileProvider.getUriForFile(this,
//                getApplicationContext().getPackageName()+".fileprovider",
//                file);
//        // create RequestBody instance from file
//        RequestBody requestFile =
//                RequestBody.create(
//                        MediaType.parse(getContentResolver().getType(fileUri)),
//                        file
//                );
//        UploadProgressRequestBody requestBody = new UploadProgressRequestBody(requestFile, new UploadProgressListener() {
//            @Override
//            public void onRequestProgress(long bytesRead, long contentLength, boolean done) {
//                Log.e("onRequestProgress","contentLength -> "+"bytesRead :"+bytesRead+",   done : "+done);
//            }
//        });
//        // MultipartBody.Part is used to send also the actual file name
//        MultipartBody.Part body =
//                MultipartBody.Part.createFormData("file", file.getName(), requestBody);
//
//        // add another part within the multipart request
////        String descriptionString = "hello, this is description speaking";
////        RequestBody description =
////                RequestBody.create(
////                        okhttp3.MultipartBody.FORM, descriptionString);
//
//
//
//        ApiRequest.get().fileUpload(observer, this, body);
////        RetrofitModule.createRetrofit("http://127.0.0.1:8080/", FileService.class, null);
//
////        final ProgressRequestBody progressRequestBody
////                = new ProgressRequestBody(
////                RequestBody.create(
////                        MediaType.parse("multipart/form-data"),
////                        tempZip
////                )
////        );
////        MultipartBody.Part part = MultipartBody.Part
////                .createFormData("file","upload",progressRequestBody);
    }

    private void nativeFileUpload(String fileName)  {
        if (observer != null && !observer.isDisposed()) {
            observer.dispose();
        }

        observer = new DisposableObserver<String>() {
            @Override
            public void onNext(String msg) {
                Log.d(MainActivity.class.getSimpleName(), msg);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(MainActivity.class.getSimpleName(), e.getMessage() + "");
            }

            @Override
            public void onComplete() {
                deleteDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                getFileList();
                Log.d(MainActivity.class.getSimpleName(), "onComplete");
            }
        };

        try {

            deleteDir(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + COMPRESS_FOLDER_NAME));
            compress();
            String zipFileName = fileZip(fileName);
            File file = new File(zipFileName);
            if (!file.exists()) {
                Log.d(MainActivity.class.getSimpleName(), "ZipFile is not exist");
            } else if (file.length() != 0 && (file.length() / 1000 / 1000) > maxFileSize) {
                Log.d(MainActivity.class.getSimpleName(), "ZipFile is big than 50MB");
            } else {
                ApiRequest.get().fileUpload(observer, this, file, true);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private UploadProgressListener uploadProgressListener = new UploadProgressListener() {
        @Override
        public void onRequestProgress(long bytesRead, long contentLength, boolean done) {
            Log.e("onRequestProgress", "contentLength -> " + "bytesRead :" + bytesRead + ",   done : " + done);
        }
    };

    private void init() {
        webViewSetting();
        findViewById(R.id.cameraOpen).setOnClickListener(this::cameraOpen);
        findViewById(R.id.fileZip).setOnClickListener(this::fileZip);
        findViewById(R.id.fileList).setOnClickListener(this::fileList);
        findViewById(R.id.deleteDirectory).setOnClickListener((v) -> {
            deleteDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            getFileList();
            getFileList();
        });
        findViewById(R.id.sizCompress).setOnClickListener((v) -> compress(currentPhotoPath));
        findViewById(R.id.fileUpload).setOnClickListener(this::fileUpload);
        findViewById(R.id.kakaomapLink).setOnClickListener(this::kakaoMapLink);
        adapter = new RecyclerImageTextAdapter();
        ((RecyclerView) findViewById(R.id.recycler1)).setAdapter(adapter);
        ((RecyclerView) findViewById(R.id.recycler1)).setLayoutManager(new LinearLayoutManager(this));
        web_view.loadUrl("file:///android_asset/sample.html");
    }

    @SuppressLint("JavascriptInterface")
    private void webViewSetting() {
        web_view = findViewById(R.id.web_view);
        WebSettings webSettings = web_view.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);
        web_view.setWebChromeClient(new WebChromeClient() { // mWebView에 WebChromeClient를 사용하도록 설정한다.
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
        web_view.addJavascriptInterface(new WebAppInterface(web_view, new WebAppInterface.WebAppInterfaceListener() {
            @Override
            public String imgDelete(String src) {
                Log.d("WebAppInterfaceListener", src);
                JSONObject jsonObject = new JSONObject();
                try {
                    File file = new File(src);
                    boolean isDelete = false;
                    if (file.exists()) {
                        isDelete = file.delete();
                    }
                    jsonObject.put("rescode", isDelete);
                    if (isDelete) {
                        jsonObject.put("resmsg", "삭제 되었습니다.");
                    } else {
                        jsonObject.put("resmsg", "삭제 실패 하였습니다.");
                    }
                    runOnUiThread(() -> getFileList());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject.toString();
            }

            @Override
            public String fileUpload(String zipFileName) {
                Log.d("WebAppInterfaceListener", zipFileName);
                JSONObject jsonObject = new JSONObject();
                try {
                    runOnUiThread(() -> nativeFileUpload(zipFileName));
                    boolean isDelete = true;
                    jsonObject.put("rescode", isDelete);
                    if (isDelete) {
                        jsonObject.put("resmsg", "upload 되었습니다.");
                    } else {
                        jsonObject.put("resmsg", "upload 실패 하였습니다.");
                    }
                    runOnUiThread(() -> getFileList());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return jsonObject.toString();
            }
        }), "Android");
//        web_view.addJavascriptInterface(new JavascriptBridgeImpl(this), "Android");
    }

    private void cameraOpen(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "카메라 권한이 허용되어 있습니다.", Toast.LENGTH_SHORT).show();
            dispatchTakePictureIntent(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }
    }

    private void kakaoMapLink(View view) {
        String kakaoMapPackage = "net.daum.android.map";
        Intent installApp = getPackageManager().getLaunchIntentForPackage(kakaoMapPackage);
        if (installApp == null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData((Uri.parse("market://details?id=" + kakaoMapPackage)));
            startActivity(intent);
        } else {
            String url = "kakaomap://open";
            url = "kakaomap://open?page=placeSearch";//장소 검색 입력 화면
            url = "kakaomap://open?page=routeSearch";//길찾기 입력 화면
            url = "kakaomap://search?q=" + ((EditText) findViewById(R.id.addressEt)).getText().toString();
//            url = "kakaomap://search?q=맛집&p=37.537229,127.005515";
//            url = "kakaomap://place?id=7813422";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private void fileZip(View view) {
        compress();
        fileZip("imgZip_0001.zip");
    }

    private void compress() {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (File f : storageDir.listFiles()) {
            String str = f.getAbsolutePath();
            if (!f.isDirectory()) {
                compress(str);
            }
        }
    }

    private String fileZip(String fileName) {
        String zipFileName = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + fileName;
        String zipMsg = fileZip(Environment.DIRECTORY_PICTURES + File.separator + COMPRESS_FOLDER_NAME, zipFileName);
        if (TextUtils.isEmpty(zipMsg)) {
            File file = new File(zipFileName);
            ((TextView) findViewById(R.id.tv_output_zipfilename)).setText(zipFileName + "(" + file.length() + "bytes)");
            return zipFileName;
        } else {
            ((TextView) findViewById(R.id.tv_output_zipfilename)).setText(zipMsg);
            return "";
        }
    }

    private String fileZip(String sourDirectory, String zipFileName) {
        File dir = getExternalFilesDir(sourDirectory);
        String files[] = dir.list();
        File[] files2 = dir.listFiles();
        ArrayList<String> filesNames = new ArrayList<>();
        for (File f : files2) {
            if (f.length() > 0 && !f.isDirectory()) {
                String str = f.getName();
                filesNames.add(dir.getPath() + File.separator + str);
            }
        }
        return zip(filesNames.toArray(new String[filesNames.size()]), zipFileName);
    }

    private void fileList(View view) {
        getFileList();
    }

    private void getFileList() {
        ((TextView) findViewById(R.id.tv_output_filenames)).setText(fileList(Environment.DIRECTORY_PICTURES));
    }

    private String fileList(String directoryPath) {
        String fileNameSize = "";
        File dir = getExternalFilesDir(directoryPath);
        String files[] = dir.list();
        File[] files2 = dir.listFiles();
        for (File f : files2) {
            String str = f.getName();
            if (f.isDirectory()) {
                System.out.print(str + "\t");
                System.out.print("DIR\n");
            } else {
                fileNameSize += str + "(" + f.length() + "bytes)" + "\r\n";
            }
        }
        return fileNameSize;
    }

    private File createImageFile(String fileName) throws IOException {
        String imageFileName = fileName + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent(String fileName) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                // Create an image file name
                photoFile = createImageFile(fileName);
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.cameratestapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String files[] = dir.list();
        File[] files2 = dir.listFiles();
        for (File f : files2) {
            String str = f.getName();
            if (f.length() == 0 && str.contains("jpg"))
                f.delete();
        }
    }

    private boolean unpackZip(String path, String zipname) {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String zip(String[] _files, String zipFileName) {
        if (_files.length > 0) {
            int BUFFER = 1024;
            BufferedInputStream origin = null;
            FileOutputStream dest = null;
            ZipOutputStream out = null;
            try {
                dest = new FileOutputStream(zipFileName);
                out = new ZipOutputStream(new BufferedOutputStream(dest));
                byte data[] = new byte[BUFFER];

                for (int i = 0; i < _files.length; i++) {
                    Log.v("Compress", "Adding: " + _files[i]);
                    FileInputStream fi = new FileInputStream(_files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);

                    ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;

                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                    fi.close();
                }
                out.close();
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        } else {
            return "Source file not found";
        }
    }

    public void unzip(String _zipFile, String _targetLocation) {

        //create target location folder if not exist
        dirChecker(_targetLocation);

        try {
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {

                //create dir if required while unzipping
                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zin.closeEntry();
                    fout.close();
                }

            }
            zin.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    private void compress(String photoPath) {
        FileOutputStream out = null;
        Bitmap uploadBitmap = null;
        Bitmap bitmap = null;
        try {
            InputStream is = getContentResolver().openInputStream(Uri.fromFile(new File(photoPath)));
            uploadBitmap = BitmapFactory.decodeStream(is);
            File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + COMPRESS_FOLDER_NAME);
            if (!dir.exists()) {
                boolean isMake = dir.mkdir();
                Log.d("isMake", isMake + "");

            }
            File file = File.createTempFile(
                    FilenameUtils.getBaseName(photoPath),  /* prefix */
                    ".jpg",         /* suffix */
                    new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + COMPRESS_FOLDER_NAME)      /* directory */
            );
            out = new FileOutputStream(file);
            int height = uploadBitmap.getHeight();
            int width = uploadBitmap.getWidth();

            if (height > 1000) {
                uploadBitmap = Bitmap.createScaledBitmap(uploadBitmap, width * 1000 / height, 1000, true);
                uploadBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            }
//            Toast.makeText(this, "make compress : "+file.getPath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            if (uploadBitmap != null) {
                if (!uploadBitmap.isRecycled()) uploadBitmap.recycle();
            }
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (Exception e1) {
                e1.getStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
//                imageView.setImageBitmap(imageBitmap);
            }
//            adapter.insertItem(new RecyclerItem().setImageFullPath(currentPhotoPath));
            getFileList();
            runOnUiThread(() -> {
                web_view.loadUrl("javascript:setImage('" + currentPhotoPath + "')");
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "카메라 권한을 허용하였습니다.", Toast.LENGTH_SHORT).show();
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "카메라 권한을 거절하였습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "카메라 권한을 다시 묻지 않음을 하였습니다.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
//                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
//            boolean isCameraPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
//            Log.d("isCameraPermission", isCameraPermission + "");
        }
    }

    private Bitmap scaleDown(Bitmap bitmap) {
        int quality;
        if (bitmap.getWidth() > 2048 && bitmap.getHeight() > 2048) {
            quality = 30;
        } else if (bitmap.getWidth() > 1024 && bitmap.getHeight() > 1024) {
            quality = 50;
        } else {
            quality = 80;
        }
        Bitmap scaleDown = Bitmap.createScaledBitmap(bitmap, (bitmap.getWidth() * quality), (bitmap.getHeight() * quality), true);
        return scaleDown;
    }
//
//    var outStream = ByteArrayOutputStream()
//bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
//
//    // 이렇게 퀄리티 다운된 비트밷은 아래와 같은 방법으로 다시 비트맵으로 만들면 됩니다.
//    val sdBitmap = BitmapFactory.decodeByteArray(outStream.toByteArray(), 0, outStream.toByteArray().size)
//
//// 사용 완료된 메모리 해제해주는 것 잊지 마시구요…
//outStream.flush()
//        bitmap.recycle()


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (observer != null && !observer.isDisposed()) {
            observer.dispose();
        }
    }

    @Override
    @JavascriptInterface
    public void log(String msg) {

    }

    @Override
    @JavascriptInterface
    public void imgDelete(String src, Object obj) {

    }
}
