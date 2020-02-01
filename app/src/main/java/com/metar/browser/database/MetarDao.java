package com.metar.browser.database;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MetarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MetarEntity message);

    @Insert
    void insertList(List<MetarEntity> messages);

    @Query("SELECT * from " + MetarEntity.TABLE_NAME + " ORDER BY " + MetarEntity.COL_STATION_NAME + " ASC")
    List<MetarEntity> getAllMessages();

    @Query("SELECT * from " + MetarEntity.TABLE_NAME + " WHERE " + MetarEntity.COL_STATION_NAME + " = :stationName")
    MetarEntity getMessage(String stationName);

    @Query("UPDATE "+ MetarEntity.TABLE_NAME + " SET "+ MetarEntity.COL_RAW_MESSAGE + " = :rawMessage, "+ MetarEntity.COL_DECODED_MESSAGE + " = :decodedMessage WHERE "+ MetarEntity.COL_STATION_NAME + " = :station")
    long updateMetarMessage(String station, String rawMessage, String decodedMessage);
}
