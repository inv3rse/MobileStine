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
        String dayStrings[] = {
                "Fr, 10. Apr. 2015",
                "Mo, 11. Mai 2015",
                "Fr, 15. Mai 2015",
                "Di, 9. Jun. 2015",
                "Do, 16. Jul. 2015",
                "Mi, 12. Nov. 2014"
        };

        for (String day : dayStrings)
        {
            boolean parsed;
            try
            {
                StineService.parseStineDate(day);
                parsed = true;
            } catch (ParseException e)
            {
                parsed = false;
            }

            assertTrue(parsed);
        }
    }
}
