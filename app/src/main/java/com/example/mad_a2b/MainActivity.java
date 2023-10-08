package com.example.mad_a2b;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    SearchResponseViewModel sViewModel;
    ImageViewModel imageViewModel;
    ErrorViewModel errorViewModel;
    UploadImageViewModel uploadImageViewModel;
    EndpointViewModel endpointViewModel;
    Button loadImage;
    Button columnType;
    TextView title;
    ProgressBar progressBar;
    EditText searchKey;
    GridLayoutManager layoutManager;
    int columnLength = 1;
    ImagesAdapter adapter;
    Toast toastMessage;
    public static boolean USEGOOGLE = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchResponseViewModel.class);
        imageViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ImageViewModel.class);
        errorViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ErrorViewModel.class);
        uploadImageViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(UploadImageViewModel.class);
        endpointViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(EndpointViewModel.class);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        requestQueue = Volley.newRequestQueue(this);
        layoutManager = new GridLayoutManager(this, columnLength);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<Picture> imageList = new ArrayList<>();
        imageList.add(new Picture(BitmapFactory.decodeResource(getResources(), R.drawable.turtle_classic),"Turtle.jpg"));
        adapter= new ImagesAdapter(imageList, columnLength,uploadImageViewModel   ); // imageList should contain your image data
        recyclerView.setAdapter(adapter);
        loadImage = findViewById(R.id.loadImage);
        title = findViewById(R.id.title);
        progressBar = findViewById(R.id.progressBarId);
        searchKey = findViewById(R.id.inputSearch);
        progressBar.setVisibility(View.INVISIBLE);
        columnType = findViewById(R.id.column);
        columnType.setBackground(ContextCompat.getDrawable(this, R.drawable.one_column));

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setVisibility(View.INVISIBLE);
                searchKey.setVisibility(View.VISIBLE);
                searchKey.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchKey, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        columnType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(columnLength == 1)
                {
                    layoutManager.setSpanCount(2);
                    columnLength = 2;
                    adapter = new ImagesAdapter(imageList, columnLength,uploadImageViewModel  );


                }
                else
                {
                    columnLength = 1;
                    layoutManager.setSpanCount(1);
                    adapter = new ImagesAdapter(imageList, columnLength ,uploadImageViewModel  );

                }
                recyclerView.setAdapter(adapter);
            }
        });
        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                if(title.getVisibility() == View.VISIBLE)
                {

                    title.setVisibility(View.INVISIBLE);
                    searchKey.setVisibility(View.VISIBLE);
                    searchKey.requestFocus();

                    imm.showSoftInput(searchKey, InputMethodManager.SHOW_IMPLICIT);
//                    if(searchKey.requestFocus()) {
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                    }
                }
                else if(title.getVisibility() == View.INVISIBLE)
                {
                    String searchValues = searchKey.getText().toString();
                    title.setText(searchValues);
                    title.setVisibility(View.VISIBLE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                    searchKey.setVisibility(View.INVISIBLE);
                    adapter.clearImages();


                    APISearchThread searchThread = new APISearchThread(searchValues,MainActivity.this,sViewModel);
                    progressBar.setVisibility(View.VISIBLE);
                    searchThread.start();
                }

            }
        });

        sViewModel.response.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                progressBar.setVisibility(View.INVISIBLE);
                showBread("Search Complete");
                System.out.println(sViewModel.getResponse());
                ImageRetrievalThread imageRetrievalThread = new ImageRetrievalThread(MainActivity.this,
                        sViewModel, imageViewModel, errorViewModel, endpointViewModel);
                progressBar.setVisibility(View.VISIBLE);
                imageRetrievalThread.start();

            }
        });

        imageViewModel.image.observe(this, new Observer<ArrayList<Picture>>() {
            @Override
            public void onChanged(ArrayList<Picture> bitmaps) {
                if (bitmaps != null && !bitmaps.isEmpty()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    adapter.setImages(bitmaps);
                    int length = endpointViewModel.getResponses().size();
                    int amount = bitmaps.size();
                    showBread(String.format("Rending %d/%d",amount, length));
                }

            }
        });
        errorViewModel.errorCode.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        uploadImageViewModel.image.observe(this, new Observer<Picture>() {
            @Override
            public void onChanged(Picture bitmap) {
                if(bitmap != null)
                {
                    showBread("uploading image to firebase");
                    ImageUploadThread imageUploadThread = new ImageUploadThread(MainActivity.this, bitmap);
                    imageUploadThread.start();
                }
            }
        });
    }

    public void showBread(String message){
        if(toastMessage!= null)
        {
            toastMessage.cancel();
        }
        toastMessage = Toast.makeText(MainActivity.this, message,Toast.LENGTH_LONG);
        toastMessage.show();
    }
}