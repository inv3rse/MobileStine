package com.inv3rs3.mobilestine.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inv3rs3.mobilestine.R;
import com.inv3rs3.mobilestine.data.Appointment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class AppointmentAdapter extends ArrayAdapter<Appointment>
{
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private final Context _context;
    private final List<Appointment> _values;

    public AppointmentAdapter(Context context, List<Appointment> values)
    {
        super(context, R.layout.appointment_item, values);
        _context = context;
        _values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) _context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.appointment_item, parent, false);

        TextView name = (TextView) rowView.findViewById(R.id.appointment_name);
        TextView time = (TextView) rowView.findViewById(R.id.appointment_time);
        TextView location = (TextView) rowView.findViewById(R.id.appointment_location);

        Appointment data = _values.get(position);
        String displayTime = dateFormat.format(data.startDate()) + " - " + dateFormat.format(data.endDate());

        name.setText(data.description());
        time.setText(displayTime);
        location.setText(data.location());

        return rowView;
    }
}