package com.sohan.attendance.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sohan.attendance.data.model.Attendance;

import java.util.List;

@Dao
public interface Attendancedao {

    // INSERT OR REPLACE — if same subjectId+date exists, it replaces it
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Attendance attendance);

    @Query("SELECT * FROM attendance WHERE subjectId = :subjectId AND date = :date LIMIT 1")
    Attendance getBySubjectAndDate(int subjectId, String date);

    @Query("SELECT * FROM attendance WHERE subjectId = :id")
    List<Attendance> getBySubject(int id);
}