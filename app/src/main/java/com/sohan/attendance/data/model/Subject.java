package com.sohan.attendance.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Subject {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public Subject(String name) {
        this.name = name;
    }
}