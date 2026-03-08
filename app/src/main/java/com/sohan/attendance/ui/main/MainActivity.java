package com.sohan.attendance.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sohan.attendance.repository.AttendanceRepository;
import com.sohan.attendance.ui.dashboard.DashboardActivity;
import com.sohan.attendance.ui.setup.SetupActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AttendanceRepository repo = new AttendanceRepository(this);

        if (repo.getSubjects().isEmpty()) {
            startActivity(new Intent(this, SetupActivity.class));
        } else {
            startActivity(new Intent(this, DashboardActivity.class));
        }

        finish();
    }
}