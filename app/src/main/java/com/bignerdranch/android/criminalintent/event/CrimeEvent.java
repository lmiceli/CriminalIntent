package com.bignerdranch.android.criminalintent.event;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO improve this design, the events surely can bew managed better
 * <p>
 * maybe rx
 * FIXME this is not used as we went for a simpler approach, might delete
 */
public class CrimeEvent {
    private static CrimeEvent sCrimeEvent;
    private List<CrimeObserver> mCrimeObservers;

    public static CrimeEvent get(Context context) {
        if (sCrimeEvent == null) {
            sCrimeEvent = new CrimeEvent(context);
        }
        return sCrimeEvent;
    }

    private CrimeEvent(Context context) {
        mCrimeObservers = new ArrayList<>();
    }

    public void crimeChanged(int position) {
        for (CrimeObserver observer : mCrimeObservers) {
            observer.onCrimeChange(position);
        }
    }

}