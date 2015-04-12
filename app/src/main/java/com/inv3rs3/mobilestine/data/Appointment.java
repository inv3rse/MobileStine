package com.inv3rs3.mobilestine.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Appointment implements Parcelable
{
    // for saving and loading from string
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

    private String _description;
    private String _location;
    private Date _start;
    private Date _end;

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

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeString(_description);
        parcel.writeString(_location);
        parcel.writeString(DATE_FORMAT.format(_start));
        parcel.writeString(DATE_FORMAT.format(_end));
    }

    // for Parcelable interface
    public static final Creator<Appointment> CREATOR = new Creator<Appointment>()
    {
        @Override
        public Appointment createFromParcel(Parcel parcel)
        {
            String description = parcel.readString();
            String location = parcel.readString();
            String startString = parcel.readString();
            String endString = parcel.readString();

            Date start = new Date();
            Date end = new Date();

            try
            {
                start = DATE_FORMAT.parse(startString);
                end = DATE_FORMAT.parse(endString);
            } catch (ParseException e)
            {
                System.out.println("could not parse date");
            }

            return new Appointment(start, end, location, description);
        }

        @Override
        public Appointment[] newArray(int size)
        {
            return new Appointment[size];
        }
    };
}
