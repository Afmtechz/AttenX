package com.sohan.attendance.ui.graph;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.sohan.attendance.R;
import com.sohan.attendance.data.database.AppDatabase;
import com.sohan.attendance.data.model.Attendance;

import java.util.*;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_graph);

        LineChart chart=findViewById(R.id.lineChart);

        AppDatabase db=AppDatabase.getInstance(this);
        List<Attendance> list=
                db.attendancedao().getBySubject(1);

        List<Entry> entries=new ArrayList<>();

        int x=0;
        for(Attendance a:list){
            entries.add(new Entry(x++,a.isPresent?1:0));
        }

        LineDataSet ds=new LineDataSet(entries,"Trend");
        chart.setData(new LineData(ds));
        chart.invalidate();
    }
}