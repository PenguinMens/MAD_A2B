package com.example.mad_a2b;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class EndpointViewModel extends ViewModel {
    public MutableLiveData<ArrayList<String>> responses;

    public EndpointViewModel() {
        this.responses =new MutableLiveData<>();
    }

    public ArrayList<String> getResponses() {
        return responses.getValue();
    }

    public void setResponses(ArrayList<String> responses) {
        this.responses.postValue(responses);
    }
}
