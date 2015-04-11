package com.inv3rs3.mobilestine.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private Date _selectedDate;
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
        _selectedDate = new Date();
        _bus = BusProvider.getInstance();
        _bus.register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            try
            {
                _selectedDate = dateFormat.parse(getArguments().getString(ARG_SELECTED_DATE));
            } catch (ParseException e)
            {
                _selectedDate = new Date();
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
                _bus.post(new RequestAppointmentsEvent(1));
            }
        });
    }

    @Subscribe
    public void onAppointmentsLoaded(AppointmentsLoadedEvent event)
    {
        AppointmentAdapter adapter = new AppointmentAdapter(getActivity(), event.appointments());
        _listView.setAdapter(adapter);
    }
}
