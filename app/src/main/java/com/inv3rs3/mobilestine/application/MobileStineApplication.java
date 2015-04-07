package com.inv3rs3.mobilestine.application;

import android.app.Application;

import com.inv3rs3.mobilestine.services.StineService;
import com.squareup.otto.Bus;

public class MobileStineApplication extends Application
{
    private Bus _bus;
    private StineService _stineService;

    @Override
    public void onCreate()
    {
        super.onCreate();

        _bus = BusProvider.getInstance();
        _stineService = new StineService(_bus);
    }
}
