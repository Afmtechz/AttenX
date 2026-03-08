package com.sohan.attendance.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sohan.attendance.data.model.Timetable;

import java.util.List;

@Dao
public interface Timetabledao {

    @Insert
    void insert(Timetable timetable);

    @Query("SELECT * FROM Timetable WHERE dayOfWeek = :day")
    List<Timetable> getByDay(String day);
}