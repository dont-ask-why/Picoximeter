package com.picoximeter.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ReadingDataBlock.class}, version = 1, exportSchema = false)
public abstract class ReadingsRoomDatabase extends RoomDatabase {
    public abstract ReadingsDAO readingsDAO();

    private static volatile ReadingsRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor
            = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static ReadingsRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null){
            synchronized (ReadingsRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ReadingsRoomDatabase.class, "readings_database").build();
                }
            }
        }
        return INSTANCE;
    }
}