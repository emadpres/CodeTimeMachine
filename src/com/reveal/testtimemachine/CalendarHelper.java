package com.reveal.testtimemachine;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CalendarHelper
{
    static String convertMonthIndexToShortName(int index_0based)
    {
        Date d = new Date(2000,index_0based,1);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(d);
        return month_name;
    }
}
