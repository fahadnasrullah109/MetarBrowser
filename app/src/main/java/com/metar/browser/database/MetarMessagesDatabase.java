package com.metar.browser.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {MetarEntity.class}, version = 1, exportSchema = false)
public abstract class MetarMessagesDatabase extends RoomDatabase {

    public abstract MetarDao metarDao();

    private static final String DATABASE_NAME = "metar_messages_database";
    private static volatile MetarMessagesDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static MetarMessagesDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MetarMessagesDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MetarMessagesDatabase.class, DATABASE_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}