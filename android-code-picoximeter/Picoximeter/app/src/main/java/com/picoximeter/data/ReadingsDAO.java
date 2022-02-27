package com.picoximeter.data;

import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReadingsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReadingDataBlock readingDataBlock);

    @Query("DELETE FROM readings_table")
    void deleteAll();

    @Query("SELECT * FROM readings_table ORDER BY id DESC")
    LiveData<List<ReadingDataBlock>> getReadings();

    @Query("SELECT DISTINCT tag FROM readings_table ORDER BY tag ASC")
    LiveData<List<String>> getTags();

    @Query("SELECT MIN(id) FROM readings_table")
    LiveData<Long> getSmallestID();

    @Query("SELECT * FROM readings_table " +
            "WHERE id > :smallestDate AND id < :largestDate AND tag IN (:tags) " +
            "AND type in (:types) ORDER BY " +
            "CASE WHEN :isAsc = 1 THEN id END ASC," +
            "CASE WHEN :isAsc = 0 THEN id END DESC")
    LiveData<List<ReadingDataBlock>> getFilteredReadings(boolean isAsc, long smallestDate, long largestDate, String[] tags, String[] types);

    @Delete
    void delete(ReadingDataBlock readingDataBlock);

    @Update
    void update(ReadingDataBlock readingDataBlock);
}