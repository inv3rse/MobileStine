package com.inv3rs3.mobilestine.application;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StineAuthenticationService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return new StineAuthenticator(this).getIBinder();
    }
}
