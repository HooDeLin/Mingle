package com.orbital2015.mingle;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class BaseApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(this, "0I7xvU5Sf08TcROv6nQRXwYpUVVxdUKjTy11nBHW", "DLFvACf2dn2Q3DpzVe7qlx2r2jR3QmTDd2cGd4Om");
        ParseFacebookUtils.initialize(this);
    }
}
