package com.inv3rs3.mobilestine.data;

import java.util.Date;

public class Appointment
{
    private Date _start;
    private Date _end;
    private String _location;
    private String _description;

    public Appointment(Date start, Date end, String location, String description)
    {
        _start = start;
        _end = end;
        _location = location;
        _description = description;
    }

    public String description()
    {
        return _description;
    }

    public String location()
    {
        return _location;
    }

    public Date startDate()
    {
        return _start;
    }

    public Date endDate()
    {
        return _end;
    }

}
