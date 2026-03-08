package com.sohan.attendance.ui.schedule;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sohan.attendance.R;
import com.sohan.attendance.data.model.Attendance;
import com.sohan.attendance.data.model.Subject;
import com.sohan.attendance.repository.AttendanceRepository;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private static final int SUBJECT_THRESHOLD = 40; // per-subject rule

    private final Context context;
    private final List<Subject> subjects;
    private String date;
    private final AttendanceRepository repository;

    public ScheduleAdapter(Context context, List<Subject> subjects,
                           String date, AttendanceRepository repository) {
        this.context    = context;
        this.subjects   = subjects;
        this.date       = date;
        this.repository = repository;
    }

    /** Called when user picks a date from the calendar */
    public void setDate(String newDate) {
        this.date = newDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_schedule_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Subject subject = subjects.get(position);
        h.txtSubjectName.setText(subject.name);
        refreshCard(h, subject);

        h.btnPresent.setOnClickListener(v -> {
            repository.markAttendance(subject.id, date, true); // REPLACE handles upsert
            refreshCard(h, subject);
        });

        h.btnAbsent.setOnClickListener(v -> {
            repository.markAttendance(subject.id, date, false);
            refreshCard(h, subject);
        });
    }

    private void refreshCard(@NonNull ViewHolder h, Subject subject) {
        // Mark state for selected date
        Attendance marked = repository.getAttendanceForDate(subject.id, date);
        if (marked == null) {
            h.btnPresent.setAlpha(0.6f);
            h.btnAbsent.setAlpha(0.6f);
            h.txtStatus.setText("Not marked");
            h.txtStatus.setTextColor(Color.parseColor("#9CA3AF"));
        } else if (marked.isPresent) {
            h.btnPresent.setAlpha(1f);
            h.btnAbsent.setAlpha(0.4f);
            h.txtStatus.setText("Present ✓");
            h.txtStatus.setTextColor(Color.parseColor("#22C55E"));
        } else {
            h.btnPresent.setAlpha(0.4f);
            h.btnAbsent.setAlpha(1f);
            h.txtStatus.setText("Absent ✗");
            h.txtStatus.setTextColor(Color.parseColor("#EF4444"));
        }

        // Overall attendance % for this subject
        List<Attendance> records = repository.getAttendance(subject.id);
        int total   = records.size();
        int present = 0;
        for (Attendance a : records) if (a.isPresent) present++;
        int pct = total == 0 ? 0 : (present * 100) / total;

        h.txtPercent.setText(pct + "%");
        if (pct >= 75)                     h.txtPercent.setTextColor(Color.parseColor("#22C55E"));
        else if (pct >= SUBJECT_THRESHOLD) h.txtPercent.setTextColor(Color.parseColor("#F97316"));
        else                               h.txtPercent.setTextColor(Color.parseColor("#EF4444"));
    }

    @Override
    public int getItemCount() { return subjects.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubjectName, txtStatus, txtPercent;
        MaterialButton btnPresent, btnAbsent;
        LinearLayout sideTab;

        ViewHolder(@NonNull View v) {
            super(v);
            txtSubjectName = v.findViewById(R.id.txtSubjectName);
            txtStatus      = v.findViewById(R.id.txtStatus);
            txtPercent     = v.findViewById(R.id.txtPercent);
            btnPresent     = v.findViewById(R.id.btnPresent);
            btnAbsent      = v.findViewById(R.id.btnAbsent);
            sideTab        = v.findViewById(R.id.sideTab);
        }
    }
}