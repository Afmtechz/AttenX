package com.sohan.attendance.utils;

import com.sohan.attendance.data.model.Attendance;

import java.util.List;

public class AttendanceUtils {

    public static int calculatePercentage(List<Attendance> records) {

        int total = records.size();
        int present = 0;

        for (Attendance a : records) {
            if (a.isPresent) present++;
        }

        if (total == 0) return 0;

        return (present * 100) / total;
    }

    public static int bunkableDays(int present, int total) {

        double required = 0.75;

        double maxAbsence = (present / required) - total;

        return (int) Math.max(maxAbsence, 0);
    }
}