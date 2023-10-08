package com.example.mad_a2b;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.time.Instant;
import java.time.LocalDate; // import the LocalDate class
import java.time.LocalDateTime;
import java.util.Random;

public class ImageUploadThread extends Thread {
    private static final String TAG = "ImageUploadTask";
    private final String endpointUrl;
    private Activity uiActivity;
    private Bitmap bitmap;
    private String name;
    private FirebaseStorage storage;

    public ImageUploadThread (Activity uiActivity, Picture picture){

        this.endpointUrl = "https://mada2b.duckdns.org/upload/";
        this.bitmap = picture.image;
        this.name = picture.name;
        this.uiActivity = uiActivity;

    }


    public void run(){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapBytes = bos.toByteArray();
        if(MainActivity.USEGOOGLE){
            storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            // need to update the datenamer refrence
            // name is currently the retreival but we want to upload with new date
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                this.name = LocalDateTime.now().toString();
            }
            else
            {
                Random rand = new Random();
                int int_random = rand.nextInt(999999999);
                this.name = Integer.toString(int_random);
            }
            StorageReference mountainsRef = storageRef.child(name + ".jpg");
            UploadTask uploadTask = mountainsRef.putBytes(bitmapBytes);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(uiActivity, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    exception.printStackTrace();
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    System.out.println(taskSnapshot.getMetadata().toString());

                    Toast.makeText(uiActivity, "Uploaded to Firebase!" , Toast.LENGTH_LONG).show();
                }
            });
        }
        else{
            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), bitmapBytes);
            builder.addFormDataPart("file", "images.jpg"  , requestBody);

            Request request = new Request.Builder()
                    .url(endpointUrl)
                    .post(builder.build())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Handle the error

                    uiActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(uiActivity, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            //errorViewModel.setErrorCode(errorViewModel.getErrorCode()+1);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Handle the responses
                    uiActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(response.code());
                            Toast.makeText(uiActivity, "Response code: " + response.code(), Toast.LENGTH_SHORT).show();

                            //errorViewModel.setErrorCode(errorViewModel.getErrorCode()+1);
                        }
                    });

                }
            });
        }

    }



    public interface UploadListener {
        void onUploadComplete(String result);
    }
}