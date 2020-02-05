package com.metar.browser.interfaces;

import com.metar.browser.database.MetarEntity;

import java.util.List;

public interface StationsNetworkCallback {

    void onStationsError();

    void onNetworkError();

    void onStationsSuccess(List<MetarEntity> stations);
}
