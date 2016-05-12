package com.bignerdranch.android.criminalintent.model;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by lmiceli on 09/05/2016.
 */
public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;

    private static DateFormat dateFormat = DateFormat.getDateTimeInstance();

    public Crime() {
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public String getFormattedDate() {
        return dateFormat.format(mDate);
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }
}
