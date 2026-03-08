package com.sohan.attendance.ui.dashboard;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sohan.attendance.R;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.VH> {

    List<String> dates;
    OnDateClick listener;

    public interface OnDateClick {
        void onClick(String date);
    }

    public DateAdapter(List<String> dates, OnDateClick listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.date_item, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        String date = dates.get(position);

        holder.txtDay.setText(date.substring(0,3));
        holder.txtDate.setText(date.substring(8));

        holder.itemView.setOnClickListener(v ->
                listener.onClick(date));
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtDay, txtDate;

        VH(View v) {
            super(v);
            txtDay = v.findViewById(R.id.txtDay);
            txtDate = v.findViewById(R.id.txtDate);
        }
    }
}