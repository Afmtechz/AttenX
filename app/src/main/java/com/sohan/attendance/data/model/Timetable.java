package com.sohan.attendance.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Timetable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String dayOfWeek;
    public int subjectId;

    public Timetable(String dayOfWeek, int subjectId) {
        this.dayOfWeek = dayOfWeek;
        this.subjectId = subjectId;
    }
}