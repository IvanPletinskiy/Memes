package com.handen.memes;

import android.app.Application;
import android.content.Context;

/**
 * Created by Vanya on 29.05.2018.
 */

public class App extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}
