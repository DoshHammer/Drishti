package com.example.drishti_2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity3 extends AppCompatActivity {
    String imgPath = null;
    String url = "http://ec2-65-2-11-52.ap-south-1.compute.amazonaws.com:5000/cd";
    private static final int request = 1;
    TextToSpeech textToSpeech;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
    }

    public void capture(View view) {
        Intent cameraInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraInt.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = getImg();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageFile != null) {
                Uri imgUri = FileProvider.getUriForFile(this, "com.example.drishti_2.Activity2", imageFile);
                cameraInt.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                startActivityForResult(cameraInt, request);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            try {
                upload(url, imgPath);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public File getImg() throws IOException {
        @SuppressLint("SimpleDateFormat") String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_").format(new Date());
        String imgName = "jpg_"+timestamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imgFile = File.createTempFile(imgName, ".jpg", storageDir);
        imgPath = imgFile.getAbsolutePath();
        return imgFile;
    }

    private void upload(String url, String Path) throws IOException, JSONException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Log.d("imagePath", Path);
        File file = new File(Path);
        RequestBody image = RequestBody.create(MediaType.parse("image/jpg"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", Path, image)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        Toast.makeText(Activity3.this, "Something went wrong:" + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        call.cancel();


                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Activity3.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(Activity3.this, response.body().string(), Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            /*textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int i) {

                                    // if No error is found then only it will run
                                    if(i!=TextToSpeech.ERROR){
                                        // To Choose language of speech
                                        textToSpeech.setLanguage(Locale.ENGLISH);
                                    }
                                }
                            });*/
                            //textToSpeech.speak(myResponse,TextToSpeech.QUEUE_FLUSH,null);
                        }
                    });
                }
            }
        });

    }
}