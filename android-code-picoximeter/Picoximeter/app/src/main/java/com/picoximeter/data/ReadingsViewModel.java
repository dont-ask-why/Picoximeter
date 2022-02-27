package com.picoximeter.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ReadingsViewModel extends AndroidViewModel {
    private ReadingsRepository mRepo;

    private final LiveData<List<ReadingDataBlock>> mReadings;

    public ReadingsViewModel(Application application){
        super(application);
        mRepo = new ReadingsRepository(application);
        mReadings = mRepo.getReadings();
    }

    public LiveData<List<ReadingDataBlock>> getReadings(){
        return mReadings;
    }

    public LiveData<List<String>> getTags() {
        return mRepo.getTags();
    }

    public LiveData<Long> getSmallestID(){
        return mRepo.getSmallestID();
    }

    public LiveData<List<ReadingDataBlock>> getFilteredReadings(boolean isAsc, long smallestDate, long largestDate, String[] tags, String[] types){
        return mRepo.getFilteredReadings(isAsc, smallestDate, largestDate, tags, types);
    }

    public void insert(ReadingDataBlock reading){
        mRepo.insert(reading);
    }

    public void delete(ReadingDataBlock reading){
        mRepo.delete(reading);
    }

    public void deleteAll(){mRepo.deleteAll();}

    public void update(ReadingDataBlock reading){
        mRepo.update(reading);
    }
}