package com.inv3rs3.mobilestine.events;

import com.inv3rs3.mobilestine.data.Appointment;

import java.util.List;

public class AppointmentsLoadedEvent
{
    private List<Appointment> _appointments;

    public AppointmentsLoadedEvent(List<Appointment> appointments)
    {
        _appointments = appointments;
    }

    public List<Appointment> appointments()
    {
        return _appointments;
    }
}
