package com.metar.browser.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.metar.browser.database.MetarEntity;
import com.metar.browser.interfaces.StationsNetworkCallback;
import com.metar.browser.repository.StationsRepository;

import java.util.List;

public class SplashActivityViewModel extends AndroidViewModel implements StationsNetworkCallback {
    private StationsRepository mRepository;
    private MutableLiveData<List<MetarEntity>> mStationsList = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> mNetworkError = new MutableLiveData<>();

    public SplashActivityViewModel(@NonNull Application application) {
        super(application);
        mRepository = new StationsRepository(application, this);
        //getStationsFromDb();
    }

    public LiveData<List<MetarEntity>> getStations() {
        return mStationsList;
    }

    public LiveData<Boolean> getNetworkError() {
        return mNetworkError;
    }

    /*private void getStationsFromDb() {
        mRepository.getStationsFromDatabase(mStationsList);
    }*/

    public LiveData<Boolean> isLoading() {
        return mIsLoading;
    }

    public void getAllStationsFromServer() {
        mIsLoading.postValue(true);
        mRepository.getStationsListFromNetwork();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mRepository.cancelAllRequests();
    }

    @Override
    public void onStationsError() {
        mIsLoading.postValue(false);
        mStationsList.postValue(null);
    }

    @Override
    public void onNetworkError() {
        mIsLoading.postValue(false);
        mNetworkError.postValue(true);
    }

    @Override
    public void onStationsSuccess(List<MetarEntity> stations) {
        mIsLoading.postValue(false);
        mStationsList.postValue(stations);
    }
}