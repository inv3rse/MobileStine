package com.inv3rs3.mobilestine.services;

import com.inv3rs3.mobilestine.events.NetworkFailureEvent;
import com.inv3rs3.mobilestine.events.RequestAppointmentsEvent;
import com.inv3rs3.mobilestine.events.RequestLoginEvent;
import com.inv3rs3.mobilestine.events.SetLoginEvent;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StineService
{
    private static final String TARGET_URL = "https://www.stine.uni-hamburg.de/scripts/mgrqispi.dll";
    private static final String COOKIE_URL = "?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N000000000000001,-N000265,-Astartseite";
    private static final String APPOINTMENT_URL = "?APPNAME=CampusNet&PRGNAME=SCHEDULER&ARGUMENTS=<ID>,-N000267,-A,-A,-N,-N000000000000000";
    private static final String LOGIN_PARAMS = "&APPNAME=CampusNet&PRGNAME=LOGINCHECK&ARGUMENTS=clino%2Cusrname%2Cpass%2Cmenuno%2Cmenu_type%2Cbrowser%2Cplatform&clino=000000000000001&menuno=000265&menu_type=classic&browser=&platform=";
    private static final String SESSION_START = "ARGUMENTS=";

    private static final MediaType MEDIA_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");

    private Bus _bus;
    private OkHttpClient _client;
    private boolean _hasCookie;

    private List<RequestAppointmentsEvent> _pendingDataRequests;

    private String _username;
    private String _password;
    private String _session;

    public StineService(Bus bus)
    {
        _bus = bus;
        _client = new OkHttpClient();
        _hasCookie = false;
        _pendingDataRequests = new ArrayList<>();

        _username = "";
        _password = "";
        _session = "";

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        _client.setCookieHandler(cookieManager);

        _bus.register(this);
    }

    @Subscribe
    public void onLogin(SetLoginEvent event)
    {
        _username = event.username();
        _password = event.password();

        getCookie();
    }

    @Subscribe
    public void onLoadAppointments(RequestAppointmentsEvent event)
    {
        _pendingDataRequests.add(event);
        getCookie();
    }

    private void getCookie()
    {
        Request cookieRequest = new Request.Builder()
                .url(TARGET_URL + COOKIE_URL)
                .build();
        _client.newCall(cookieRequest).enqueue(new CustomCallback()
        {
            @Override
            void onSuccess(Response response)
            {
                _hasCookie = true;
                getSession();
            }
        });

    }

    private void getSession()
    {
        if (_username.isEmpty() || _password.isEmpty())
        {
            _bus.post(new RequestLoginEvent());
            return;
        }

        if (!_hasCookie)
        {
            getCookie();
            return;
        }

        RequestBody body = RequestBody.create(MEDIA_URLENCODED,
                "usrname=" + _username + "&pass=" + _password +LOGIN_PARAMS);

        Request sessionRequest = new Request.Builder()
                .url(TARGET_URL)
                .post(body)
                .build();
        _client.newCall(sessionRequest).enqueue(new CustomCallback()
        {
            @Override
            void onSuccess(Response response)
            {
                boolean loginFailed = true;
                String refreshParam = response.header("REFRESH", "failed");
                if (!refreshParam.equals("failed"))
                {
                    int start = refreshParam.indexOf(SESSION_START);
                    int end = refreshParam.indexOf(",", start);

                    if (start != -1 && end != -1 && start < end)
                    {
                        _session = refreshParam.substring(start + SESSION_START.length(), end);
                        loginFailed = false;
                    }
                }

                if (loginFailed)
                {
                    _username = "";
                    _password = "";
                    _session = "";
                    _bus.post(new RequestLoginEvent());
                }
                else if (!_pendingDataRequests.isEmpty())
                {
                    getWeek(_pendingDataRequests.get(0));
                }
            }
        });

    }

    private void getWeek(RequestAppointmentsEvent timespan)
    {

    }

    abstract class CustomCallback implements Callback
    {
        @Override
        public void onFailure(Request request, IOException e)
        {
            _bus.post(new NetworkFailureEvent(e.toString()));
            _pendingDataRequests.clear();
        }

        @Override
        public void onResponse(Response response)
        {
            if (!response.isSuccessful())
            {
                _bus.post(new NetworkFailureEvent("unexpected server response " + response.code()));
                return;
            }

            System.out.println(response.toString());
            onSuccess(response);
        }

        abstract void onSuccess(Response response);
    }
}
