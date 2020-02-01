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
    private MutableLiveData<List<MetarEntity>> mStations = new MutableLiveData<>();
    private MutableLiveData<MetarEntity> mEntity = new MutableLiveData<>();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MetarRepository(application);
    }

    public LiveData<List<MetarEntity>> getAllStations() {
        return mStations;
    }

    public void getAllStationsFromDatabase() {
        mRepository.getAllStations(mStations);
    }

    public LiveData<MetarEntity> getMetarMessage() {
        return mEntity;
    }

    public void getStationDetailFromServer(String station) {
        mRepository.getDetail(station, mEntity);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mRepository.cancelAllRequests();
    }
}