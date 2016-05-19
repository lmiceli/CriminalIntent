package com.bignerdranch.android.criminalintent.model;

/**
 * Created by lmiceli on 19/05/2016.
 */
public class Utils {

    public static boolean isEmptyString(String text) {
        return (text == null || text.trim().equals("null") || text.trim()
                .length() <= 0);
    }
}
