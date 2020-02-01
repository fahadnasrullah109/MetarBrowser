package com.metar.browser.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.metar.browser.database.MetarEntity;
import com.metar.browser.repository.StationsRepository;

import java.util.List;

public class SplashActivityViewModel extends AndroidViewModel {
    private StationsRepository mRepository;
    private MutableLiveData<List<MetarEntity>> mStationsList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public SplashActivityViewModel(@NonNull Application application) {
        super(application);
        mRepository = new StationsRepository(application);
        getStationsFromDb();
    }

    public LiveData<List<MetarEntity>> getStations() {
        return mStationsList;
    }

    private void getStationsFromDb(){
        mRepository.getStationsFromDatabase(mStationsList);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public void getAllStationsFromServer(String url) {
        mRepository.getStationsListFromNetwork(url, mStationsList);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mRepository.cancelAllRequests();
    }
}