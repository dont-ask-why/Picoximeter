package com.picoximeter.data;

import android.icu.util.Calendar;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "readings_table")
public class ReadingDataBlock {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "hr")
    private int hr;

    @ColumnInfo(name = "spo2")
    private int spo2;

    @NonNull
    @ColumnInfo(name = "tag")
    private String tag;

    public ReadingDataBlock(long id, int hr, int spo2, @NonNull String tag){
        this.id = id;
        this.hr = hr;
        this.spo2 = spo2;
        this.tag = tag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Calendar getCalender(){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(id);
        return c;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    @NonNull
    public String getTag() {
        return tag;
    }

    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }
}
