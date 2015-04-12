package com.inv3rs3.mobilestine.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import com.inv3rs3.mobilestine.R;
import com.inv3rs3.mobilestine.application.BusProvider;
import com.inv3rs3.mobilestine.events.AppointmentsLoadedEvent;
import com.inv3rs3.mobilestine.events.RequestAppointmentsEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppointmentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppointmentsFragment extends Fragment
{

    private static final String ARG_SELECTED_DATE = "selected date";
    private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private Calendar _selectedDate;
    private ListView _listView;
    private Bus _bus;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param selectedDate Date to show appointments for.
     * @return A new instance of fragment AppointmentsFragment.
     */
    public static AppointmentsFragment newInstance(Date selectedDate)
    {
        AppointmentsFragment fragment = new AppointmentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_DATE, dateFormat.format(selectedDate));
        fragment.setArguments(args);
        return fragment;
    }

    public AppointmentsFragment()
    {
        _selectedDate = Calendar.getInstance();
        _bus = BusProvider.getInstance();
        _bus.register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null)
        {
            try
            {
                _selectedDate.setTime(dateFormat.parse(getArguments().getString(ARG_SELECTED_DATE)));
            } catch (ParseException e)
            {
                _selectedDate = Calendar.getInstance();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View createdView = inflater.inflate(R.layout.fragment_appointments, container, false);
        setUpUi(createdView);
        return createdView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        _bus.unregister(this);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    private void setUpUi(View searchView)
    {
        _listView = (ListView) searchView.findViewById(R.id.appointments_list);
        Button refreshBtn = (Button) searchView.findViewById(R.id.appointments_refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                _bus.post(new RequestAppointmentsEvent(_selectedDate.getTime(), 7));
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.appointments, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_select_date)
        {
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker picker, int year, int month, int day)
                    {
                        _selectedDate.set(Calendar.YEAR, year);
                        _selectedDate.set(Calendar.MONTH, month);
                        _selectedDate.set(Calendar.DAY_OF_MONTH, day);

                        _bus.post(new RequestAppointmentsEvent(_selectedDate.getTime(), 7));
                    }
                },
                _selectedDate.get(Calendar.YEAR),
                _selectedDate.get(Calendar.MONTH),
                _selectedDate.get(Calendar.DAY_OF_MONTH));

            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onAppointmentsLoaded(AppointmentsLoadedEvent event)
    {
        AppointmentAdapter adapter = new AppointmentAdapter(getActivity(), event.appointments());
        _listView.setAdapter(adapter);
    }
}
