package com.inv3rs3.mobilestine.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;

import com.inv3rs3.mobilestine.R;
import com.inv3rs3.mobilestine.application.BusProvider;
import com.inv3rs3.mobilestine.data.Appointment;
import com.inv3rs3.mobilestine.events.AppointmentsLoadedEvent;
import com.inv3rs3.mobilestine.events.RequestAppointmentsEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentsFragment extends Fragment
{

    private static final String ARG_SELECTED_DATE = "selected date";
    private static final String ARG_APPOINTMENTS = "appointments";
    private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

    private Calendar _selectedDate;
    private SwipeRefreshLayout _refreshLayout;
    private ListView _listView;
    private List<Appointment> _appointments;
    private Bus _bus;

    public AppointmentsFragment()
    {
        _appointments = null;
        _selectedDate = Calendar.getInstance();
        _bus = BusProvider.getInstance();
        _bus.register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null)
        {
            _appointments = savedInstanceState.getParcelableArrayList(ARG_APPOINTMENTS);
            try
            {
                _selectedDate.setTime(dateFormat.parse(savedInstanceState.getString(ARG_SELECTED_DATE)));
            } catch (ParseException ignored)
            {
            }
        } else
        {
            System.out.println("no saved instance");
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
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_SELECTED_DATE, dateFormat.format(_selectedDate.getTime()));
        outState.putParcelableArrayList(ARG_APPOINTMENTS, (java.util.ArrayList<? extends Parcelable>) _appointments);
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
        _refreshLayout = (SwipeRefreshLayout) searchView.findViewById(R.id.fragment_appointments);
        _listView = (ListView) searchView.findViewById(R.id.appointments_list);

        if (_appointments != null && !_appointments.isEmpty())
        {
            AppointmentAdapter adapter = new AppointmentAdapter(getActivity(), _appointments);
            _listView.setAdapter(adapter);
        }
        else
        {
            refreshData();
        }

        _refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                refreshData();
            }
        });
    }

    private void refreshData()
    {
        loadData(_selectedDate.getTime());
    }

    private void loadData(Date date)
    {
        _refreshLayout.setRefreshing(true);
        _bus.post(new RequestAppointmentsEvent(date, 7));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.appointments, menu);
    }

    @Override
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

                    refreshData();
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
        _appointments = event.appointments();
        AppointmentAdapter adapter = new AppointmentAdapter(getActivity(), event.appointments());
        _listView.setAdapter(adapter);
        _refreshLayout.setRefreshing(false);
    }
}
