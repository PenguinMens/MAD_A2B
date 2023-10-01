package com.example.mad_a2b;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class UploadImageViewModel extends ViewModel {
    public MutableLiveData<Picture> image;

    public UploadImageViewModel(){
        image = new MutableLiveData<Picture>();

    }

    public Picture getImage(){
        return image.getValue();
    }
    public void setImage(Picture bitmap){
        image.postValue(bitmap);
    }
}
