package com.sohan.attendance.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sohan.attendance.data.model.Subject;

import java.util.List;

@Dao
public interface Subjectdao {

    @Insert
    void insert(Subject subject);

    @Query("SELECT * FROM Subject")
    List<Subject> getAll();
}