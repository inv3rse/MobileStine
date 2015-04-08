package com.inv3rs3.mobilestine.application;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.otto.Bus;

public final class BusProvider {
    private static final Object sLock = new Object();
    private static MyBus sInstance;

    public static MyBus getInstance() {

        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new MyBus(new Handler(Looper.getMainLooper()));
                Log.i("BusProvider", "The bus was ready.");
            }
            return sInstance;
        }
    }

    private BusProvider() {
        // No instances.
    }

    public static class MyBus extends Bus {
        private Handler mHandler;

        public MyBus(Handler handler) {
            super();
            mHandler = handler;
        }

        @Override
        public void post(final Object event) {
            if (Looper.myLooper() != mHandler.getLooper()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyBus.super.post(event);
                    }
                });
            } else {
                super.post(event);
            }
        }
    }
}