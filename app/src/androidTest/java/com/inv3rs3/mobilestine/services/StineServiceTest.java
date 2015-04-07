package com.inv3rs3.mobilestine.services;

import com.squareup.otto.Bus;

import junit.framework.TestCase;

/**
 * Created by dennis on 08.04.15.
 */
public class StineServiceTest extends TestCase
{
    private Bus _bus;
    private StineService _stineService;

    @Override
    protected void setUp()
    {
        _bus = new Bus();
    }
}
