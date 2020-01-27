package com.metar.browser.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = MetarEntity.TABLE_NAME)
public class MetarEntity {
    public static final String TABLE_NAME = "metar_messages_table";
    public static final String COL_STATION_NAME = "station_name";
    public static final String COL_DECODED_MESSAGE = "decodedMessage";
    public static final String COL_RAW_MESSAGE = "rawMessage";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = MetarEntity.COL_STATION_NAME)
    private String mStation;

    @ColumnInfo(name = MetarEntity.COL_DECODED_MESSAGE)
    private String mDecodedMessage;

    @ColumnInfo(name = MetarEntity.COL_RAW_MESSAGE)
    private String mRawMessage;

    public MetarEntity() {
        // Needed Default constructor for db
    }

    public MetarEntity(String stationName, String decodedMessage, String rawMessage) {
        this.mStation = stationName;
        this.mDecodedMessage = decodedMessage;
        this.mRawMessage = rawMessage;
    }

    public String getStation() {
        return mStation;
    }

    public void setStation(String mStation) {
        this.mStation = mStation;
    }

    public String getDecodedMessage() {
        return mDecodedMessage;
    }

    public void setDecodedMessage(String mDecodedMessage) {
        this.mDecodedMessage = mDecodedMessage;
    }

    public String getRawMessage() {
        return mRawMessage;
    }

    public void setRawMessage(String mRawMessage) {
        this.mRawMessage = mRawMessage;
    }
}
