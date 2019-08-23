package com.android.yuniapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.model.EachDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class GridCellAdapter extends BaseAdapter {
    private Context mContext;
    private List<EachDate> m_cObjList;
    private static final int DAY_OFFSET = 1;
    private int m_cDaysInMonth;
    private TextView txtGrid;
    private final String[] m_cMonths = {"Jan", "Feb", "Mar",
            "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec"};
    private final int[] m_cDaysOfMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private int m_cToday_Date, m_cMonth_Now, m_cYear_Now, m_cMonth, m_cYear;
    private Calendar calendar;

    public GridCellAdapter(Context context, int month, int year, int currentMonth, int currentYear) {
        super();
        this.mContext = context;
        this.m_cObjList = new ArrayList<EachDate>();

        m_cMonth = currentMonth;
        m_cYear = currentYear;
        printMonth(month, year);
    }

    @Override
    public int getCount() {
        return m_cObjList.size();
    }

    @Override
    public Object getItem(int position) {
        return m_cObjList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.adapter_grid_item, parent, false);
        txtGrid = view.findViewById(R.id.txt_grid);
        EachDate eachDate = m_cObjList.get(position);
        String[] day = eachDate.m_cDate.split("-");

        txtGrid.setText(day[0]);
        String date = m_cObjList.get(position).m_cDate;
        txtGrid.setTag(date);

        if (eachDate.m_cColor.equals("GREY")) {
            txtGrid.setTextColor(Color.LTGRAY);
        } else if (eachDate.m_cColor.equals("WHITE")) {
            txtGrid.setTextColor(Color.BLACK);
        } else if (eachDate.m_cColor.equals("BLUE")) {
            txtGrid.setTextColor(ContextCompat.getColor(mContext, R.color.colorBlue));
        } else if (eachDate.m_cColor.equals("RED")) {
            txtGrid.setTextColor(Color.RED);
        }


        return view;
    }
    public int getGridSize() {
        return m_cObjList.size();
    }

    public EachDate getGridItem(int pPos) {
        try {
            return m_cObjList.get(pPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void printMonth(int mm, int yy) {
        // The number of days to leave blank at
        // the start of this month.
        int lTrailingSpaces = 0;
        int lDaysInPrevMonth = 0;
        int lPrevMonth = 0;
        int lPrevYear = 0;
        int lNextMonth = 0;
        int lNextYear = 0;

        int lCurrentMonth = mm;
        m_cDaysInMonth = getNumberOfDaysOfMonth(lCurrentMonth);

        // Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
        GregorianCalendar cal = new GregorianCalendar(yy, lCurrentMonth, 1); //changing the value to 0 - starts day from
        // monday changing the value to 1 - starts day from sunday and so on..

        if (lCurrentMonth == 11) {
            lPrevMonth = lCurrentMonth - 1;
            lDaysInPrevMonth = getNumberOfDaysOfMonth(lPrevMonth);
            lNextMonth = 0;
            lPrevYear = yy;
            lNextYear = yy + 1;
        } else if (lCurrentMonth == 0) {
            lPrevMonth = 11;
            lPrevYear = yy - 1;
            lNextYear = yy;
            lDaysInPrevMonth = getNumberOfDaysOfMonth(lPrevMonth);
            lNextMonth = 1;
        } else {
            lPrevMonth = lCurrentMonth - 1;
            lNextMonth = lCurrentMonth + 1;
            lNextYear = yy;
            lPrevYear = yy;
            lDaysInPrevMonth = getNumberOfDaysOfMonth(lPrevMonth);
        }

        // Compute how much to leave before before the first day of the
        // month.
        // getDay() returns 0 for Sunday.
        int lCurrentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        lTrailingSpaces = lCurrentWeekDay;

        if (cal.isLeapYear(cal.get(Calendar.YEAR)) && mm == 1) {
            ++m_cDaysInMonth;
        }

        // Trailing Month days
        EachDate lDay = null;
        for (int i = 0; i < lTrailingSpaces; i++) {
            lDay = new EachDate();
            int lMonth = lPrevMonth + 1;
            String lMonthSt = null;
            if (lMonth <= 9) {
                lMonthSt = "0" + lMonth;
            } else {
                lMonthSt = "" + lMonth;
            }
            lDay.m_cFormatDate = String.format("%d-%s-%d", lPrevYear, lMonthSt, (lDaysInPrevMonth - lTrailingSpaces + DAY_OFFSET) + i);
            String lDate = String.valueOf((lDaysInPrevMonth - lTrailingSpaces + DAY_OFFSET) + i) + "-" + getMonthAsString(lPrevMonth) + "-" + lPrevYear;
            lDay.m_cDate = lDate;
            lDay.m_cColor = "GREY";
            m_cObjList.add(lDay);

        }
        calendar = Calendar.getInstance(Locale.getDefault());
        m_cToday_Date = calendar.get(Calendar.DAY_OF_MONTH);
        m_cMonth_Now = calendar.get(Calendar.MONTH);
        m_cYear_Now = calendar.get(Calendar.YEAR);

        for (int i = 1; i <= m_cDaysInMonth; i++) {
            lDay = new EachDate();
            int lMonth = lCurrentMonth + 1;
            String lMonthSt = null;
            if (lMonth <= 9) {
                lMonthSt = "0" + lMonth;
            } else {
                lMonthSt = "" + lMonth;
            }
            String lday = null;
            if (i <= 9) {
                lday = "0" + i;
            } else {
                lday = "" + i;
            }
            lDay.m_cFormatDate = String.format("%d-%s-%s", yy, lMonthSt, lday);
            String lDate = String.valueOf(i) + "-" + getMonthAsString(lCurrentMonth) + "-" + yy;

            lDay.m_cDate = lDate;
            if (i == m_cToday_Date && m_cMonth_Now == m_cMonth && m_cYear_Now == m_cYear) {
                lDay.m_cColor = "BLUE";
            } else {
                lDay.m_cColor = "WHITE";
            }

                /*if(null != m_cAbsents && m_cAbsents.containsKey(lDay.m_cFormatDate)) {
                    lDay.m_cColor = "YELLOW" ;
                }*/
            m_cObjList.add(lDay);
        }

        for (int i = 0; i < m_cObjList.size() % 7; i++) {
            lDay = new EachDate();
            int lMonth = lNextMonth + 1;
            String lMonthSt = null;
            if (lMonth <= 9) {
                lMonthSt = "0" + lMonth;
            } else {
                lMonthSt = "" + lMonth;
            }
            String lDate = String.valueOf(i + 1) + "-" + getMonthAsString(lNextMonth) + "-" + lNextYear;
            lDay.m_cFormatDate = String.format("%d-%s-%d", lNextYear, lMonthSt, i + 1);
            lDay.m_cDate = lDate;
            lDay.m_cColor = "GREY";
            m_cObjList.add(lDay);
        }

    }
    public void setGridItem(int pPos, EachDate lItem) {
        m_cObjList.set(pPos, lItem);
    }
    private String getMonthAsString(int i) {
        return m_cMonths[i];
    }

    private int getNumberOfDaysOfMonth(int i) {
        return m_cDaysOfMonth[i];
    }


}
