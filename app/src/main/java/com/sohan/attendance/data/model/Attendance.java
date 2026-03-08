package com.sohan.attendance.data.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "attendance",
        indices = {@Index(value = {"subjectId", "date"}, unique = true)}
)
public class Attendance {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int subjectId;
    public String date;
    public boolean isPresent;

    public Attendance(int subjectId, String date, boolean isPresent) {
        this.subjectId = subjectId;
        this.date      = date;
        this.isPresent = isPresent;
    }
}