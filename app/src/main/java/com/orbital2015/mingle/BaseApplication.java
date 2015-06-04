package com.orbital2015.mingle;

import android.app.Application;

import com.parse.Parse;

public class BaseApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        Parse.initialize(this, "app-id", "client-key");
    }
}
