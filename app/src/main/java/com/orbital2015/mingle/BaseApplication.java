package com.orbital2015.mingle;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;

public class BaseApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

                                               @Override
                                               public void onActivityCreated(Activity activity,
                                                                             Bundle savedInstanceState) {

                                                   // new activity created; force its orientation to portrait
                                                   activity.setRequestedOrientation(
                                                           ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                                               }

                                               @Override
                                               public void onActivityStarted(Activity activity) {

                                               }

                                               @Override
                                               public void onActivityResumed(Activity activity) {

                                               }

                                               @Override
                                               public void onActivityPaused(Activity activity) {

                                               }

                                               @Override
                                               public void onActivityStopped(Activity activity) {

                                               }

                                               @Override
                                               public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                                               }

                                               @Override
                                               public void onActivityDestroyed(Activity activity) {

                                               }
                                           });

        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(this, "0I7xvU5Sf08TcROv6nQRXwYpUVVxdUKjTy11nBHW", "DLFvACf2dn2Q3DpzVe7qlx2r2jR3QmTDd2cGd4Om");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseFacebookUtils.initialize(this);
    }
}
