package com.sohan.attendance.repository;

import android.content.Context;

import com.sohan.attendance.data.database.AppDatabase;
import com.sohan.attendance.data.model.*;

import java.util.List;

public class AttendanceRepository {

    private final AppDatabase db;

    public AttendanceRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public void addSubject(String name) {
        db.subjectdao().insert(new Subject(name));
    }

    public List<Subject> getSubjects() {
        return db.subjectdao().getAll();
    }

    public void addTimetable(String day, int subjectId) {
        db.timetabledao().insert(new Timetable(day, subjectId));
    }

    public List<Timetable> getTimetable(String day) {
        return db.timetabledao().getByDay(day);
    }

    public void markAttendance(int subjectId, String date, boolean present) {
        db.attendancedao().insert(new Attendance(subjectId, date, present));
    }

    public Attendance getAttendanceForDate(int subjectId, String date) {
        return db.attendancedao().getBySubjectAndDate(subjectId, date);
    }

    public List<Attendance> getAttendance(int subjectId) {
        return db.attendancedao().getBySubject(subjectId);
    }
}