package com.example.finalproject.ui;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.models.TransactionModel;
import com.example.finalproject.database.DatabaseHelper;
import com.example.finalproject.utils.BottomNavHelper;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        calendarView = findViewById(R.id.calendarView);
        dbHelper = new DatabaseHelper(this);

        decorateCalendar(); // initial setup
        BottomNavHelper.setup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        decorateCalendar(); // refresh decorations when coming back
    }

    private void decorateCalendar() {
        // Clear existing decorators (avoid stacking dots)
        calendarView.removeDecorators();

        // Pull transactions from DB
        List<TransactionModel> transactions = dbHelper.getAllTransactions();

        // Separate income & expense dates
        Set<CalendarDay> incomeDays = new HashSet<>();
        Set<CalendarDay> expenseDays = new HashSet<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (TransactionModel t : transactions) {
            try {
                Date date = sdf.parse(t.getDate());
                if (date == null) continue;

                CalendarDay day = CalendarDay.from(date);
                if ("income".equalsIgnoreCase(t.getType())) {
                    incomeDays.add(day);
                } else if ("expense".equalsIgnoreCase(t.getType())) {
                    expenseDays.add(day);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Apply decorators again
        if (!incomeDays.isEmpty()) {
            calendarView.addDecorator(new EventDecorator(Color.parseColor("#00C853"), incomeDays)); // green
        }
        if (!expenseDays.isEmpty()) {
            calendarView.addDecorator(new EventDecorator(Color.parseColor("#FF1744"), expenseDays)); // red
        }
    }

    // Dot decorator class
    public static class EventDecorator implements DayViewDecorator {
        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(10, color)); // 10 = dot radius
        }
    }
}