package com.metar.browser.database;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MetarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MetarEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<MetarEntity> messages);

    @Query("SELECT * from metar_messages_table ORDER BY " + MetarEntity.COL_STATION_NAME + " ASC")
    LiveData<List<MetarEntity>> getAllMessages();

    @Query("SELECT * from metar_messages_table ORDER BY " + MetarEntity.COL_STATION_NAME + " ASC")
    Cursor getMessagesCursor();

    @Query("SELECT * from metar_messages_table WHERE " + MetarEntity.COL_STATION_NAME + " = :stationName")
    LiveData<MetarEntity> getMessage(String stationName);
}
