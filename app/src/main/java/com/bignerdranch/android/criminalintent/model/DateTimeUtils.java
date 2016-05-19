package com.bignerdranch.android.criminalintent.model;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Date utilities depending on context to ensure it goes with user settings.
 */
public class DateTimeUtils {

    public static String formatDate (Context context, Date date) {
        return DateFormat.getLongDateFormat(context).format(date);
    }

    public static String formatTime (Context context, Date date) {
        return DateFormat.getTimeFormat(context).format(date);
    }

    /**
     * TODO enhance this as is only concatenation as of now
     * @param context
     * @param date
     * @return
     */
    public static String formatDateTime (Context context, Date date) {
        return DateFormat.getLongDateFormat(context).format(date) + " " +
                DateFormat.getTimeFormat(context).format(date);
    }

    public static boolean is24HourFormat (Context context) {
        return DateFormat.is24HourFormat(context);
    }

    public static Date getTime(int hour, int minute, int second){

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.clear(); // set to defaults.

        setTimeInCalendar(calendar, hour, minute, second);

        return calendar.getTime();
    }

    private static void setTimeInCalendar(GregorianCalendar calendar, int hour, int minute, int second) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
    }

    public static Date getTime(int hour, int minute){

        return getTime(hour, minute, 0);
    }

    /**
     * all values from date, but hour minute and second which will came from time
     * @param date
     * @param time
     * @return
     */
    public static Date mergeDateAndTime(Date date, Date time){
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        GregorianCalendar timeCalendar = new GregorianCalendar();
        timeCalendar.setTime(time);

        int hour = timeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = timeCalendar.get(Calendar.MINUTE);
        int second = timeCalendar.get(Calendar.SECOND);

        setTimeInCalendar(calendar, hour, minute, second);

        return calendar.getTime();
    }


}
