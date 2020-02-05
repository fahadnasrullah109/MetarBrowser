package com.metar.browser.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.metar.browser.database.MetarEntity;
import com.metar.browser.interfaces.MetarNetworkCallback;
import com.metar.browser.repository.MetarRepository;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel implements MetarNetworkCallback {
    private MetarRepository mRepository;
    private MutableLiveData<List<MetarEntity>> mStations = new MutableLiveData<>();
    private MutableLiveData<MetarEntity> mEntity = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MetarRepository(application, this);
    }

    public LiveData<List<MetarEntity>> getAllStations() {
        return mStations;
    }

    public void getAllStationsFromDatabase() {
        mRepository.getAllStations();
    }

    public LiveData<MetarEntity> getMetarMessage() {
        return mEntity;
    }

    public LiveData<Boolean> getIsLoading() {
        return mIsLoading;
    }

    public void getStationDetailFromServer(String station) {
        mIsLoading.postValue(true);
        mRepository.getDetail(station);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mRepository.cancelAllRequests();
    }

    @Override
    public void onMessageError() {
        mIsLoading.postValue(false);
        mEntity.postValue(null);
    }

    @Override
    public void onMessageSuccess(MetarEntity entity) {
        mIsLoading.postValue(false);
        mEntity.postValue(entity);
    }

    @Override
    public void onStationsSuccess(List<MetarEntity> stations) {
        mStations.postValue(stations);
    }
}