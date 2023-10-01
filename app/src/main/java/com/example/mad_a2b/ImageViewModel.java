package com.example.mad_a2b;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ImageViewModel extends ViewModel {
    public MutableLiveData<ArrayList<Picture>> image;
    public ImageViewModel(){
        image = new MutableLiveData<ArrayList<Picture>>();
        image.setValue(new ArrayList<>());
    }

    public ArrayList<Picture> getImage(){
        return image.getValue();
    }
    public void setImage(ArrayList<Picture> bitmap){
        image.postValue(bitmap);
    }
}
