package com.sohan.attendance.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.sohan.attendance.repository.AttendanceRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.sohan.attendance.R;
import com.sohan.attendance.data.model.Attendance;
import com.sohan.attendance.data.model.Subject;
import com.sohan.attendance.repository.AttendanceRepository;
import com.sohan.attendance.ui.dashboard.DashboardActivity;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    // UI
    private TextInputEditText etSubjectName;
    private ChipGroup chipGroupSubjects;
    private Spinner spinnerDay;
    private Spinner spinnerSubject;
    private LinearLayout layoutTimetablePreview;
    private TextView txtNoSlots;
    private MaterialButton btnDone;

    // Data
    private AttendanceRepository repository;
    private final List<String> subjectNames = new ArrayList<>();
    private ArrayAdapter<String> subjectSpinnerAdapter;

    // Timetable slots (day → subject)
    private final List<String[]> timetableSlots = new ArrayList<>();

    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        repository = new AttendanceRepository(this);

        // Bind views
        etSubjectName        = findViewById(R.id.etSubjectName);
        chipGroupSubjects    = findViewById(R.id.chipGroupSubjects);
        spinnerDay           = findViewById(R.id.spinnerDay);
        spinnerSubject       = findViewById(R.id.spinnerSubject);
        layoutTimetablePreview = findViewById(R.id.layoutTimetablePreview);
        txtNoSlots           = findViewById(R.id.txtNoSlots);
        btnDone              = findViewById(R.id.btnDone);

        MaterialButton btnAddSubject     = findViewById(R.id.btnAddSubject);
        MaterialButton btnAddToTimetable = findViewById(R.id.btnAddToTimetable);

        // Day spinner
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, DAYS);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        // Subject spinner (dynamic)
        subjectSpinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, subjectNames);
        subjectSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectSpinnerAdapter);

        // ── Add Subject ──
        btnAddSubject.setOnClickListener(v -> {
            String name = etSubjectName.getText() != null
                    ? etSubjectName.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name)) {
                etSubjectName.setError("Enter a subject name");
                return;
            }
            if (subjectNames.contains(name)) {
                Toast.makeText(this, "Subject already added", Toast.LENGTH_SHORT).show();
                return;
            }

            subjectNames.add(name);
            subjectSpinnerAdapter.notifyDataSetChanged();
            addSubjectChip(name);
            etSubjectName.setText("");
        });

        // ── Add to Timetable ──
        btnAddToTimetable.setOnClickListener(v -> {
            if (subjectNames.isEmpty()) {
                Toast.makeText(this, "Add at least one subject first", Toast.LENGTH_SHORT).show();
                return;
            }
            String day     = DAYS[spinnerDay.getSelectedItemPosition()];
            String subject = subjectNames.get(spinnerSubject.getSelectedItemPosition());

            timetableSlots.add(new String[]{day, subject});
            refreshTimetablePreview();
            Toast.makeText(this, day + " → " + subject + " added", Toast.LENGTH_SHORT).show();
        });

        // ── Done ──
        btnDone.setOnClickListener(v -> {
            if (subjectNames.isEmpty()) {
                Toast.makeText(this, "Add at least one subject to continue", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save subjects to DB
            for (String name : subjectNames) {
                repository.addSubject(name);
            }

            // Save timetable slots to DB
            List<Subject> savedSubjects = repository.getSubjects();
            for (String[] slot : timetableSlots) {
                String day     = slot[0];
                String subName = slot[1];
                for (Subject s : savedSubjects) {
                    if (s.name.equals(subName)) {
                        repository.addTimetable(day, s.id);
                        break;
                    }
                }
            }

            // Navigate to Dashboard
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
    }

    private void addSubjectChip(String name) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.chip_bg); // define in colors.xml
        chip.setTextColor(getColor(android.R.color.white));
        chip.setOnCloseIconClickListener(v -> {
            subjectNames.remove(name);
            subjectSpinnerAdapter.notifyDataSetChanged();
            chipGroupSubjects.removeView(chip);
        });
        chipGroupSubjects.addView(chip);
    }

    private void refreshTimetablePreview() {
        layoutTimetablePreview.removeAllViews();

        if (timetableSlots.isEmpty()) {
            txtNoSlots.setVisibility(View.VISIBLE);
            return;
        }

        txtNoSlots.setVisibility(View.GONE);

        for (String[] slot : timetableSlots) {
            TextView row = new TextView(this);
            row.setText("  " + slot[0] + "  →  " + slot[1]);
            row.setTextColor(0xFFF8FAFC);
            row.setTextSize(14f);
            row.setPadding(0, 8, 0, 8);
            layoutTimetablePreview.addView(row);

            // Divider
            View divider = new View(this);
            divider.setBackgroundColor(0xFF1E293B);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(lp);
            layoutTimetablePreview.addView(divider);
        }
    }
}