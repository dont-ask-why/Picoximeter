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

    @ColumnInfo(name = "systolic")
    private int systolic;

    @ColumnInfo(name = "diastolic")
    private int diastolic;

    @ColumnInfo(name = "tag")
    private String tag;

    @ColumnInfo(name = "type")
    private String type;

    public ReadingDataBlock(long id, int hr, int spo2, int systolic, int diastolic, @NonNull String tag, @NonNull String type){
        this.id = id;
        this.hr = hr;
        this.spo2 = spo2;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.tag = tag;
        this.type = type;
    }

    public Calendar getCalender(){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(id);
        return c;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getSystolic() {
        return systolic;
    }

    public void setSystolic(int systolic) {
        this.systolic = systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(int diastolic) {
        this.diastolic = diastolic;
    }

    @NonNull
    public String getTag() {
        return tag;
    }

    public void setTag(@NonNull String tags) {
        this.tag = tags;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }
}
