package com.inv3rs3.mobilestine.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.inv3rs3.mobilestine.R;
import com.inv3rs3.mobilestine.data.Appointment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

class AppointmentAdapter extends ArrayAdapter<Appointment>
{
    private static final DateFormat HOUR_FORMAT = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE d. LLL. yyyy", Locale.GERMAN);

    private final Context _context;
    private final List<ViewItem> _values;

    public AppointmentAdapter(Context context, List<Appointment> values)
    {
        super(context, R.layout.appointment_card, values);
        _context = context;
        _values = toViewItems(values);
    }

    @Override
    public int getCount()
    {
        return _values.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) _context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewItem item = _values.get(position);
        View rowView;

        if (item.isTime())
        {
            rowView = inflater.inflate(R.layout.time_card, parent, false);
            TextView date = (TextView) rowView.findViewById(R.id.time_date);
            date.setText(DATE_FORMAT.format(item.time()));
        }
        else
        {
            rowView = inflater.inflate(R.layout.appointment_card, parent, false);
            TextView name = (TextView) rowView.findViewById(R.id.appointment_name);
            TextView time = (TextView) rowView.findViewById(R.id.appointment_time);
            TextView location = (TextView) rowView.findViewById(R.id.appointment_location);

            Appointment data = item.appointment();
            String displayTime = HOUR_FORMAT.format(data.startDate()) + " - " + HOUR_FORMAT.format(data.endDate());

            name.setText(data.description());
            time.setText(displayTime);
            location.setText(data.location());
        }

        return rowView;
    }

    private List<ViewItem> toViewItems(List<Appointment> appointments)
    {
        ArrayList<ViewItem> viewItems = new ArrayList<>();
        GregorianCalendar currDay = new GregorianCalendar();

        for (int i = 0; i < appointments.size(); i++)
        {
            GregorianCalendar thisDate = new GregorianCalendar();
            thisDate.setTime(appointments.get(i).startDate());

            if (i == 0 || thisDate.get(Calendar.DAY_OF_YEAR) != currDay.get(Calendar.DAY_OF_YEAR))
            {
                currDay = thisDate;
                viewItems.add(new ViewItem(currDay.getTime()));
            }

            viewItems.add(new ViewItem(appointments.get(i)));
        }

        return viewItems;
    }

    private class ViewItem
    {
        private Appointment _appointment;
        private boolean _isTime;
        private Date _time;

        public ViewItem(Appointment appointment)
        {
            _appointment = appointment;
            _isTime = false;
            _time = null;
        }

        public ViewItem(Date date)
        {
            _time = date;
            _isTime = true;
            _appointment = null;
        }

        public boolean isTime()
        {
            return _isTime;
        }

        public Date time()
        {
            return _time;
        }

        public Appointment appointment()
        {
            return _appointment;
        }
    }
}