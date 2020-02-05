package com.metar.browser.interfaces;

import com.metar.browser.database.MetarEntity;

import java.util.List;

public interface MetarNetworkCallback {

    void onMessageError();

    void onMessageSuccess(MetarEntity entity);

    void onStationsSuccess(List<MetarEntity> stations);
}
