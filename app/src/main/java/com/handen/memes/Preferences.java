package com.handen.memes;

import android.content.Context;

/**
 * Created by Vanya on 30.05.2018.
 */

public class Preferences {

    static String PERIOD = "period";
    private Context mContext;
    static android.content.SharedPreferences mSharedPreferences;
    static android.content.SharedPreferences.Editor editor;

    public Preferences() {
        mContext = App.getContext();
        mSharedPreferences = mContext.getSharedPreferences("HandenMemes", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }

    public static long getPeriod() {
        return mSharedPreferences.getLong(PERIOD,1800000);
    }

    public static void setPeriod(long newPeriod) {
        editor.putLong(PERIOD, newPeriod);
        editor.commit();
    }
}
