package com.sohan.attendance.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.sohan.attendance.data.dao.*;
import com.sohan.attendance.data.model.*;

@Database(entities = {Subject.class, Attendance.class, Timetable.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract Subjectdao subjectdao();
    public abstract Attendancedao attendancedao();
    public abstract Timetabledao timetabledao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "attendance_db"
            ).fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }
        return instance;
    }
}