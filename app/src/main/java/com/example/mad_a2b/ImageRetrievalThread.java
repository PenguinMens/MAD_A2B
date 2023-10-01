package com.example.mad_a2b;
import java.time.LocalDateTime;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class ImageRetrievalThread extends Thread {

    private RemoteUtilities remoteUtilities;
    private SearchResponseViewModel sViewModel;
    private ImageViewModel imageViewModel;
    private ErrorViewModel errorViewModel;
    private Activity uiActivity;

    public ImageRetrievalThread(Activity uiActivity, SearchResponseViewModel viewModel, ImageViewModel imageViewModel, ErrorViewModel errorViewModel) {
        remoteUtilities = RemoteUtilities.getInstance(uiActivity);
        this.sViewModel = viewModel;
        this.imageViewModel = imageViewModel;
        this.errorViewModel = errorViewModel;
        this.uiActivity=uiActivity;
    }
    public void run(){
        ArrayList<String> endpoints = getEndpoints(sViewModel.getResponse());
        if(endpoints.size() == 0){
            uiActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(uiActivity,"No image found",Toast.LENGTH_LONG).show();
                    errorViewModel.setErrorCode(errorViewModel.getErrorCode()+1);
                }
            });
        }
        else {
            ArrayList<Picture> image = new ArrayList<>();
            for (String endpoint : endpoints) {
                System.out.println(endpoint);
                Bitmap bitmap = getImageFromUrl(endpoint);
                Picture picture = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    picture = new Picture(bitmap, LocalDateTime.now().toString());
                }
                else
                {
                    Random rand = new Random();
                    int int_random = rand.nextInt(999999999);
                    picture = new Picture(bitmap, Integer.toString(int_random));
                }

                image.add(picture);
                imageViewModel.setImage(image);
            }
//            try {
//                Thread.sleep(3000);
//            } catch (Exception e) {
//            }
            imageViewModel.setImage(image);
        }
    }

    private ArrayList<String> getEndpoints(String data){
        ArrayList<String> imageUrl = new ArrayList<>();
        try {
            JSONObject jBase = new JSONObject(data);
            JSONArray jHits = jBase.getJSONArray("hits");
            if(jHits.length()>0){
                System.out.printf("Jhits length %d%n",jHits.length());
                int i = 0;
                int end = 15;
                if(jHits.length()<15)
                    end = jHits.length();
                while( i < end)
                {
                    JSONObject jHitsItem = jHits.getJSONObject(i);
                    System.out.println(jHitsItem.getString("previewURL"));
                    imageUrl.add(jHitsItem.getString("previewURL"));
                    i++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imageUrl;
    }

    private Bitmap getImageFromUrl(String imageUrl){
        Bitmap image = null;
        Uri.Builder url = Uri.parse(imageUrl).buildUpon();
        String urlString = url.build().toString();
        HttpURLConnection connection = remoteUtilities.openConnection(urlString);
        if(connection!=null){
            if(remoteUtilities.isConnectionOkay(connection)==true){
                image = getBitmapFromConnection(connection);
                connection.disconnect();
            }
        }
        return image;
    }

    public Bitmap getBitmapFromConnection(HttpURLConnection conn){
        Bitmap data = null;
        try {
            InputStream inputStream = conn.getInputStream();
            byte[] byteData = getByteArrayFromInputStream(inputStream);
            data = BitmapFactory.decodeByteArray(byteData,0,byteData.length);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return data;
    }

    private byte[] getByteArrayFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

}
