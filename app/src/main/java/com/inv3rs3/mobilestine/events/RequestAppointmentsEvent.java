package com.inv3rs3.mobilestine.events;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RequestAppointmentsEvent
{
    private Date _startDate;
    private Date _endDate;

    public RequestAppointmentsEvent(Date start, Date end)
    {
        _startDate = start;
        _endDate = end;
    }

    public RequestAppointmentsEvent(int daysFromNow)
    {
        _startDate = new Date();

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(_startDate);
        calendar.add(Calendar.DAY_OF_MONTH, daysFromNow);

        _endDate = calendar.getTime();
    }

    public Date startDate()
    {
        return _startDate;
    }

    public Date endDate()
    {
        return _endDate;
    }
}
