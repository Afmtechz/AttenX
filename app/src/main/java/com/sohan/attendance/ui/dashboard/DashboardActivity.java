package com.sohan.attendance.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.sohan.attendance.R;
import com.sohan.attendance.data.model.Attendance;
import com.sohan.attendance.data.model.Subject;
import com.sohan.attendance.repository.AttendanceRepository;
import com.sohan.attendance.ui.schedule.ScheduleActivity;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private static final int OVERALL_THRESHOLD = 75;
    private static final int SUBJECT_THRESHOLD = 40;

    private TextView txtOverallPercent, txtPresentTotal, txtAbsentTotal, txtTotalClasses;
    private TextView txtOverallBunk;
    private LinearLayout layoutSubjectCards;
    private AttendanceRepository repository;

    // Totals needed for overall bunk calc — populated in updateDashboard()
    private int grandTotalPresent = 0;
    private int grandTotalClasses = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        repository         = new AttendanceRepository(this);
        txtOverallPercent  = findViewById(R.id.txtOverallPercent);
        txtPresentTotal    = findViewById(R.id.txtPresentTotal);
        txtAbsentTotal     = findViewById(R.id.txtAbsentTotal);
        txtTotalClasses    = findViewById(R.id.txtTotalClasses);
        txtOverallBunk     = findViewById(R.id.txtOverallBunk);
        layoutSubjectCards = findViewById(R.id.layoutSubjectCards);

        MaterialButton btnViewSchedule = findViewById(R.id.btnViewSchedule);
        btnViewSchedule.setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
    }

    private void updateDashboard() {
        List<Subject> subjects = repository.getSubjects();
        grandTotalPresent = 0;
        int totalAbsent   = 0;
        grandTotalClasses = 0;

        layoutSubjectCards.removeAllViews();

        // First pass — collect totals and per-subject data
        int[][] subjectStats = new int[subjects.size()][2]; // [present, total]
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            List<Attendance> records = repository.getAttendance(s.id);
            int p = 0;
            for (Attendance r : records) { if (r.isPresent) p++; }
            int t = records.size();
            int a = t - p;
            subjectStats[i][0] = p;
            subjectStats[i][1] = t;
            grandTotalPresent += p;
            totalAbsent       += a;
            grandTotalClasses += t;
        }

        // Overall %
        int overallPct = grandTotalClasses == 0 ? 0
                : (grandTotalPresent * 100) / grandTotalClasses;
        txtOverallPercent.setText(overallPct + "%");
        txtPresentTotal.setText(grandTotalPresent + " Present");
        txtAbsentTotal.setText(totalAbsent + " Absent");
        txtTotalClasses.setText(grandTotalClasses + " Total");

        if (overallPct >= OVERALL_THRESHOLD)      txtOverallPercent.setTextColor(Color.parseColor("#4ADE80"));
        else if (overallPct >= 60)                txtOverallPercent.setTextColor(Color.parseColor("#FACC15"));
        else                                      txtOverallPercent.setTextColor(Color.parseColor("#F87171"));

        // Overall bunk info (75% rule on total)
        int overallCanBunk = calcCanBunk(grandTotalPresent, grandTotalClasses, OVERALL_THRESHOLD);
        int overallNeed    = calcNeedAttend(grandTotalPresent, grandTotalClasses, OVERALL_THRESHOLD);
        if (grandTotalClasses == 0) {
            txtOverallBunk.setText("No classes recorded yet");
            txtOverallBunk.setTextColor(Color.parseColor("#94A3B8"));
        } else if (overallPct >= OVERALL_THRESHOLD) {
            txtOverallBunk.setText("✅ Can bunk " + overallCanBunk + " more overall (75% rule)");
            txtOverallBunk.setTextColor(Color.parseColor("#4ADE80"));
        } else {
            txtOverallBunk.setText("🚨 Attend " + overallNeed + " more to reach 75% overall");
            txtOverallBunk.setTextColor(Color.parseColor("#F87171"));
        }

        // Second pass — per-subject cards
        for (int i = 0; i < subjects.size(); i++) {
            int present = subjectStats[i][0];
            int total   = subjectStats[i][1];
            addSubjectCard(subjects.get(i).name, present, total);
        }
    }

    /**
     * Bunk calculator that respects BOTH thresholds simultaneously.
     *
     * We can bunk X more classes if:
     *   (1) Subject: present / (total + X) >= 0.40
     *   (2) Overall: (grandPresent) / (grandTotal + X) >= 0.75
     *       [bunking means grandPresent stays same, grandTotal increases]
     *
     * We take the MINIMUM of both limits.
     */
    private void addSubjectCard(String name, int present, int total) {
        int subjectPct = total == 0 ? 0 : (present * 100) / total;
        float dp = getResources().getDisplayMetrics().density;

        // Limit from subject 40% rule
        int limitSubject = calcCanBunk(present, total, SUBJECT_THRESHOLD);

        // Limit from overall 75% rule
        // If we bunk X more of THIS subject: grandPresent unchanged, grandTotal += X
        int limitOverall = calcCanBunkOverall(grandTotalPresent, grandTotalClasses, OVERALL_THRESHOLD);

        // The real bunk limit is the most restrictive
        int canBunk = Math.min(limitSubject, limitOverall);

        // How many to attend — need to satisfy BOTH rules
        int needForSubject = calcNeedAttend(present, total, SUBJECT_THRESHOLD);
        int needForOverall = calcNeedAttend(grandTotalPresent, grandTotalClasses, OVERALL_THRESHOLD);
        int needAttend     = Math.max(needForSubject, needForOverall);

        // Build card
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.setMargins(0, 0, 0, (int)(12 * dp));
        card.setLayoutParams(cardLp);
        card.setRadius(16 * dp);
        card.setCardElevation(3 * dp);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding((int)(16*dp), (int)(16*dp), (int)(16*dp), (int)(16*dp));

        // Row 1: name + %
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        TextView txtName = new TextView(this);
        txtName.setText(name);
        txtName.setTextSize(15f);
        txtName.setTypeface(null, Typeface.BOLD);
        txtName.setTextColor(Color.parseColor("#1F2937"));
        txtName.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView txtPct = new TextView(this);
        txtPct.setText(subjectPct + "%");
        txtPct.setTextSize(16f);
        txtPct.setTypeface(null, Typeface.BOLD);
        if (subjectPct >= 75)                     txtPct.setTextColor(Color.parseColor("#22C55E"));
        else if (subjectPct >= SUBJECT_THRESHOLD) txtPct.setTextColor(Color.parseColor("#F97316"));
        else                                      txtPct.setTextColor(Color.parseColor("#EF4444"));

        row1.addView(txtName);
        row1.addView(txtPct);

        // Row 2: counts
        int absent = total - present;
        TextView txtCounts = new TextView(this);
        txtCounts.setText(present + " present  •  " + absent + " absent  •  " + total + " total");
        txtCounts.setTextSize(13f);
        txtCounts.setTextColor(Color.parseColor("#6B7280"));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cLp.setMargins(0, (int)(6*dp), 0, 0);
        txtCounts.setLayoutParams(cLp);

        // Row 3: bunk info — considers BOTH rules
        TextView txtBunk = new TextView(this);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        bLp.setMargins(0, (int)(8*dp), 0, 0);
        txtBunk.setLayoutParams(bLp);
        txtBunk.setTextSize(13f);

        if (total == 0) {
            txtBunk.setText("No classes recorded yet");
            txtBunk.setTextColor(Color.parseColor("#9CA3AF"));
        } else if (subjectPct >= SUBJECT_THRESHOLD && canBunk > 0) {
            String limitedBy = (limitOverall < limitSubject) ? " (limited by 75% overall)" : " (40% subject rule)";
            txtBunk.setText("✅ Can bunk " + canBunk + " more" + limitedBy);
            txtBunk.setTextColor(Color.parseColor("#22C55E"));
        } else if (subjectPct >= SUBJECT_THRESHOLD && canBunk == 0) {
            txtBunk.setText("⚠️ Next absence drops you below threshold");
            txtBunk.setTextColor(Color.parseColor("#F97316"));
        } else {
            String limitedBy = (needForOverall > needForSubject) ? " (for 75% overall)" : " (for 40% subject)";
            txtBunk.setText("🚨 Attend " + needAttend + " more" + limitedBy);
            txtBunk.setTextColor(Color.parseColor("#EF4444"));
        }

        inner.addView(row1);
        inner.addView(txtCounts);
        inner.addView(txtBunk);
        card.addView(inner);
        layoutSubjectCards.addView(card);
    }

    // ── Math helpers ──────────────────────────────────────────────────────────

    /**
     * How many classes can be bunked while keeping present/total >= threshold%?
     * present / (total + X) >= threshold/100
     * X <= (present * 100/threshold) - total
     */
    private int calcCanBunk(int present, int total, int threshold) {
        if (total == 0 || present == 0) return 0;
        int max = (int) Math.floor((present * 100.0 / threshold) - total);
        return Math.max(max, 0);
    }

    /**
     * For overall bunk: bunking means total goes up but present stays same.
     * grandPresent / (grandTotal + X) >= threshold/100
     * Same formula as calcCanBunk.
     */
    private int calcCanBunkOverall(int grandPresent, int grandTotal, int threshold) {
        return calcCanBunk(grandPresent, grandTotal, threshold);
    }

    /**
     * How many consecutive classes must be attended to reach threshold%?
     * (present + X) / (total + X) >= threshold/100
     * X >= (threshold * total - 100 * present) / (100 - threshold)
     */
    private int calcNeedAttend(int present, int total, int threshold) {
        if (total == 0) return 0;
        int pct = (present * 100) / total;
        if (pct >= threshold) return 0;
        double need = (threshold * total - 100.0 * present) / (100.0 - threshold);
        return (int) Math.ceil(need);
    }
}