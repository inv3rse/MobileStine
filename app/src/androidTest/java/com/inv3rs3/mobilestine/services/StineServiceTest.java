package com.inv3rs3.mobilestine.services;

import junit.framework.TestCase;

import java.text.ParseException;


public class StineServiceTest extends TestCase
{
    @Override
    protected void setUp()
    {
    }

    public void testTimeParse()
    {
        String dayString = "Fr, 10. Apr. 2015";
        boolean parsed = false;

        try
        {
            StineService.STINE_DAY_FORMAT.parse(dayString);
            parsed = true;
        } catch (ParseException e)
        {
            parsed = false;
        }

        assertTrue(parsed);
    }
}
