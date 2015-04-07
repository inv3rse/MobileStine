package com.inv3rs3.mobilestine.application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

// Provided by Square under the Apache License
public final class BusProvider
{
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}
