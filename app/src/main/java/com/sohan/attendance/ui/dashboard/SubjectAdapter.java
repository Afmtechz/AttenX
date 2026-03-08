package com.sohan.attendance.ui.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sohan.attendance.R;
import com.sohan.attendance.data.model.Attendance;
import com.sohan.attendance.data.model.Subject;
import com.sohan.attendance.repository.AttendanceRepository;
import com.sohan.attendance.utils.AttendanceUtils;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {

    private final Context context;
    private final List<Subject> subjectList;
    private final AttendanceRepository repository;
    private final String selectedDate;

    public SubjectAdapter(Context context, List<Subject> subjectList, String selectedDate) {
        this.context = context;
        this.subjectList = subjectList;
        this.selectedDate = selectedDate;
        this.repository = new AttendanceRepository(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.attendance_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Subject subject = subjectList.get(position);
        holder.txtSubject.setText(subject.name);

        List<Attendance> records = repository.getAttendance(subject.id);
        int percent = AttendanceUtils.calculatePercentage(records);
        holder.txtPercent.setText("Attendance: " + percent + "%");

        boolean alreadyMarked = false;
        boolean present = false;

        for (Attendance a : records) {
            if (a.date.equals(selectedDate)) {
                alreadyMarked = true;
                present = a.isPresent;
                break;
            }
        }

        if (alreadyMarked) {
            if (present) {
                holder.imgStatus.setImageResource(android.R.drawable.checkbox_on_background);
                holder.imgStatus.setColorFilter(Color.parseColor("#22C55E"));
            } else {
                holder.imgStatus.setImageResource(android.R.drawable.ic_delete);
                holder.imgStatus.setColorFilter(Color.parseColor("#EF4444"));
            }
        } else {
            holder.imgStatus.setImageResource(android.R.drawable.ic_menu_help);
            holder.imgStatus.setColorFilter(Color.parseColor("#9CA3AF"));
        }

        holder.itemView.setOnClickListener(v -> {

            // 🔥 Re-check inside click (no lambda final issue now)
            List<Attendance> updatedRecords = repository.getAttendance(subject.id);

            for (Attendance a : updatedRecords) {
                if (a.date.equals(selectedDate)) {
                    return; // already marked
                }
            }

            repository.markAttendance(subject.id, selectedDate, true);

            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtSubject, txtPercent;
        ImageView imgStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtPercent = itemView.findViewById(R.id.txtPercent);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }
}