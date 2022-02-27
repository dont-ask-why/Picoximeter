package com.picoximeter.data;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

public class ReadingsRepository {
    private ReadingsDAO mReadingsDao;
    private LiveData<List<ReadingDataBlock>> mReadings;
    private ReadingsRoomDatabase db;

    ReadingsRepository(Application application){
        db = ReadingsRoomDatabase.getDatabase(application);
        mReadingsDao = db.readingsDAO();
        mReadings = mReadingsDao.getReadings();
    }

    LiveData<List<String>> getTags(){
        return mReadingsDao.getTags();
    }

    LiveData<Long> getSmallestID() {
        return mReadingsDao.getSmallestID();
    }

    LiveData<List<ReadingDataBlock>> getReadings(){
        return mReadings;
    }

    LiveData<List<ReadingDataBlock>> getFilteredReadings(boolean isAsc, long smallestDate, long largestDate, String[] tags){
        return mReadingsDao.getFilteredReadings(isAsc, smallestDate, largestDate, tags);
    }

    void insert(ReadingDataBlock reading){
        ReadingsRoomDatabase.databaseWriteExecutor.execute(() -> {
            mReadingsDao.insert(reading);
        });
    }

    void deleteAll(){
        ReadingsRoomDatabase.databaseWriteExecutor.execute(() -> {
            mReadingsDao.deleteAll();
        });
    }

    void delete(ReadingDataBlock reading){
        ReadingsRoomDatabase.databaseWriteExecutor.execute(() -> {
            mReadingsDao.delete(reading);
        });
    }

    void update(ReadingDataBlock reading){
        ReadingsRoomDatabase.databaseWriteExecutor.execute(() -> {
            mReadingsDao.update(reading);
        });
    }
}