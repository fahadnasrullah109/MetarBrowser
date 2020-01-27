package com.metar.browser.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.metar.browser.database.MetarEntity;
import com.metar.browser.repository.MetarRepository;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {
    private MetarRepository mRepository;
    private LiveData<List<MetarEntity>> mAllMessages;
    private LiveData<MetarEntity> mEntity = new MutableLiveData<>();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MetarRepository(application);
        mAllMessages = mRepository.getAllMessages();
    }

    public LiveData<List<MetarEntity>> getAllMessages() {
        return mAllMessages;
    }

    /*public void getAllStationsFromServer(String url) {
        mRepository.getMessagesFromNetwork(url);
    }

    public void getStationDetailFromServer(String url) {
        mRepository.getMessagesFromNetwork(url);
    }*/

    @Override
    protected void onCleared() {
        super.onCleared();
        mRepository.cancelAllRequests();
    }
}
