package com.reveal.testtimemachine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarHelper
{
    static String convertDateToString_(Date d)
    {
        // Create an instance of SimpleDateFormat used for formatting
        // the string representation of date (month/day/year)
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy"); //"MM/dd/yyyy HH:mm:ss"

        // Get the date today using Calendar object.
        Date today = Calendar.getInstance().getTime();
        // Using DateFormat format method we can create a string
        // representation of a date with the defined format.
        String reportDate = df.format(today);

        return reportDate;
    }

    static String convertDateToString(Date d)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        String result = cal.get(Calendar.YEAR)+" "+convertMonthIndexToShortName(cal.get(Calendar.MONTH))+" "+cal.get(Calendar.DAY_OF_MONTH);
        return result;
    }

    static String convertMonthIndexToShortName(int index_0based)
    {
        Date d = new Date(2000,index_0based,1);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(d);
        return month_name;
    }
}
