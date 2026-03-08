package com.sohan.attendance.ui.schedule;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sohan.attendance.R;
import com.sohan.attendance.data.model.Subject;
import com.sohan.attendance.data.model.Timetable;
import com.sohan.attendance.repository.AttendanceRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private LinearLayout layoutDayStrip;
    private HorizontalScrollView dayScrollView;
    private RecyclerView recyclerSchedule;
    private TextView txtMonthYear;
    private TextView txtSelectedDate;

    private AttendanceRepository repository;
    private ScheduleAdapter adapter;

    // The currently selected date (drives both strip highlight AND marking)
    private Calendar selectedCal;

    // The "anchor" Monday of the week currently shown in the strip
    private Calendar weekAnchor;

    private final String[] DAY_SHORT = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    // DAY_OF_WEEK values for Mon-Sat
    private final int[] DOW = {
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        repository       = new AttendanceRepository(this);
        layoutDayStrip   = findViewById(R.id.layoutDayStrip);
        dayScrollView    = findViewById(R.id.dayScrollView);
        recyclerSchedule = findViewById(R.id.recyclerSchedule);
        txtMonthYear     = findViewById(R.id.txtMonthYear);
        txtSelectedDate  = findViewById(R.id.txtSelectedDate);

        // Start on today
        selectedCal = Calendar.getInstance();
        weekAnchor  = getWeekMonday(selectedCal);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCalendar).setOnClickListener(v -> showDatePicker());

        refresh();
    }

    // ── Show date picker ──────────────────────────────────────────────────────

    private void showDatePicker() {
        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, day);
                    // Move the week strip anchor to the week containing picked date
                    weekAnchor = getWeekMonday(selectedCal);
                    refresh();
                },
                selectedCal.get(Calendar.YEAR),
                selectedCal.get(Calendar.MONTH),
                selectedCal.get(Calendar.DAY_OF_MONTH)
        );
        dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
        dlg.show();
    }

    // ── Refresh everything after any date change ───────────────────────────────

    private void refresh() {
        updateHeader();
        buildDayStrip();
        loadSubjectsForSelectedDay();
    }

    private void updateHeader() {
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMM yyyy", Locale.US);
        SimpleDateFormat dayFmt   = new SimpleDateFormat("dd MMM", Locale.US);
        txtMonthYear.setText(monthFmt.format(selectedCal.getTime()));
        txtSelectedDate.setText(dayFmt.format(selectedCal.getTime()));
    }

    // ── Day strip: shows Mon-Sat of the week containing selectedCal ───────────

    private void buildDayStrip() {
        layoutDayStrip.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;

        // selectedCal's date string for comparison
        String selectedDateStr = toDateString(selectedCal);

        for (int i = 0; i < DAY_SHORT.length; i++) {
            // Build the Calendar for this strip slot
            Calendar slotCal = (Calendar) weekAnchor.clone();
            slotCal.add(Calendar.DAY_OF_YEAR, i); // Mon+0, Mon+1 ... Mon+5

            String slotDateStr = toDateString(slotCal);
            boolean isSelected = slotDateStr.equals(selectedDateStr);

            LinearLayout dayView = new LinearLayout(this);
            dayView.setOrientation(LinearLayout.VERTICAL);
            dayView.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    (int)(72 * dp), LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins((int)(4*dp), (int)(8*dp), (int)(4*dp), (int)(8*dp));
            dayView.setLayoutParams(lp);

            if (isSelected) {
                dayView.setBackgroundResource(R.drawable.day_selected_bg);
            }

            // Short day name
            TextView txtShort = new TextView(this);
            txtShort.setText(DAY_SHORT[i]);
            txtShort.setTextSize(12f);
            txtShort.setGravity(Gravity.CENTER);
            txtShort.setTextColor(isSelected ? Color.parseColor("#1F2937") : Color.WHITE);
            dayView.addView(txtShort);

            // Day number
            TextView txtNum = new TextView(this);
            txtNum.setText(String.valueOf(slotCal.get(Calendar.DAY_OF_MONTH)));
            txtNum.setTextSize(18f);
            txtNum.setTypeface(null, Typeface.BOLD);
            txtNum.setGravity(Gravity.CENTER);
            txtNum.setTextColor(isSelected ? Color.parseColor("#1F2937") : Color.WHITE);
            dayView.addView(txtNum);

            // Click — switch selected date to this slot
            final Calendar tapped = (Calendar) slotCal.clone();
            dayView.setOnClickListener(v -> {
                selectedCal = (Calendar) tapped.clone();
                refresh();
            });

            layoutDayStrip.addView(dayView);
        }

        // Auto-scroll so selected day is centred
        dayScrollView.post(() -> {
            int idx = getSelectedStripIndex(selectedDateStr);
            if (idx >= 0) {
                int childWidth = (int)(80 * dp); // 72dp + 8dp margins
                int scroll = idx * childWidth
                        - dayScrollView.getWidth() / 2
                        + childWidth / 2;
                dayScrollView.smoothScrollTo(Math.max(scroll, 0), 0);
            }
        });
    }

    private int getSelectedStripIndex(String selectedDateStr) {
        for (int i = 0; i < 6; i++) {
            Calendar slotCal = (Calendar) weekAnchor.clone();
            slotCal.add(Calendar.DAY_OF_YEAR, i);
            if (toDateString(slotCal).equals(selectedDateStr)) return i;
        }
        return -1;
    }

    // ── Load subjects for the selected day name ───────────────────────────────

    private void loadSubjectsForSelectedDay() {
        // Day name from selectedCal
        String dayName = new SimpleDateFormat("EEEE", Locale.US)
                .format(selectedCal.getTime());
        String dateStr = toDateString(selectedCal);

        List<Timetable> slots   = repository.getTimetable(dayName);
        List<Subject>   allSubs = repository.getSubjects();
        List<Subject>   daySubs = new ArrayList<>();

        for (Timetable slot : slots) {
            for (Subject s : allSubs) {
                if (s.id == slot.subjectId) { daySubs.add(s); break; }
            }
        }

        adapter = new ScheduleAdapter(this, daySubs, dateStr, repository);
        recyclerSchedule.setLayoutManager(new LinearLayoutManager(this));
        recyclerSchedule.setAdapter(adapter);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns the Monday of the week containing the given Calendar */
    private Calendar getWeekMonday(Calendar cal) {
        Calendar monday = (Calendar) cal.clone();
        monday.set(Calendar.HOUR_OF_DAY, 0);
        monday.set(Calendar.MINUTE, 0);
        monday.set(Calendar.SECOND, 0);
        monday.set(Calendar.MILLISECOND, 0);
        // DAY_OF_WEEK: Sun=1, Mon=2 ... so diff = dow - 2 (or +5 if Sunday)
        int dow  = monday.get(Calendar.DAY_OF_WEEK);
        int diff = (dow == Calendar.SUNDAY) ? -6 : -(dow - Calendar.MONDAY);
        monday.add(Calendar.DAY_OF_YEAR, diff);
        return monday;
    }

    private String toDateString(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
    }
}