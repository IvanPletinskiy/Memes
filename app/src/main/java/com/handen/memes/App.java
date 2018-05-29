package com.handen.memes;

import android.app.Application;
import android.content.Context;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

/**
 * Created by Vanya on 29.05.2018.
 */

public class App extends Application {
    private static Context mContext;

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                // VKAccessToken is invalid
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        VKSdk.initialize(getApplicationContext());

    }

    public static Context getContext(){
        return mContext;
    }
}
