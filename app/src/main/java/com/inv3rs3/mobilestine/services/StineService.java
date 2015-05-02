package com.inv3rs3.mobilestine.services;

import com.inv3rs3.mobilestine.data.Appointment;
import com.inv3rs3.mobilestine.data.StineAuthToken;
import com.inv3rs3.mobilestine.events.AppointmentsLoadedEvent;
import com.inv3rs3.mobilestine.events.NetworkFailureEvent;
import com.inv3rs3.mobilestine.events.RequestAppointmentsEvent;
import com.inv3rs3.mobilestine.events.RequestTokenEvent;
import com.inv3rs3.mobilestine.events.SetTokenEvent;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StineService
{
    public static final String TARGET_URL = "https://www.stine.uni-hamburg.de/scripts/mgrqispi.dll";
    private static final String DATA_URL = "?APPNAME=CampusNet&PRGNAME=SCHEDULERPRINT&ARGUMENTS=<SESSION>,-N000363,-A<DATE>,-A,-N1";

    private static final String DATA_EXPRESSION = "(<td width=\"590\" class=\"tbhead\" colspan=\"100%\" style=\"font-size:13px;\">(.*))</td>|(<td width=\".*\" class=\"tbdata\">(.*)</td>)";
    private static final Pattern DATA_PATTERN = Pattern.compile(DATA_EXPRESSION);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    private static final SimpleDateFormat STINE_DAY_FORMAT = new SimpleDateFormat("EEE, d. LLL. yyyy", Locale.GERMAN);

    private Bus _bus;
    private OkHttpClient _client;
    private CookieManager _cookieManager;

    private StineAuthToken _token;
    // If we need to ask for auth token
    private RequestAppointmentsEvent _pendingRequest;

    public StineService(Bus bus)
    {
        _bus = bus;
        _client = new OkHttpClient();
        _cookieManager = new CookieManager();

        _token = null;
        _pendingRequest = null;

        _cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        _client.setCookieHandler(_cookieManager);

        _bus.register(this);
    }

    @Subscribe
    public void onSetToken(SetTokenEvent event)
    {
        _token = event.token();
        System.out.println("setting " + _token.cookies().size() + " cookies");
        for (HttpCookie cookie : _token.cookies())
        {
            _cookieManager.getCookieStore().add(StineAuthToken.COOKIE_URI, cookie);
        }

        if (_pendingRequest != null)
        {
            getWeek(_pendingRequest);
        }
    }

    @Subscribe
    public void getWeek(RequestAppointmentsEvent timespan)
    {
        if (_token == null)
        {
            _bus.post(new RequestTokenEvent());
            _pendingRequest = timespan;
            return;
        }

        String dateSelection = DATE_FORMAT.format(timespan.startDate());
        String dataUrl = DATA_URL.replace("<SESSION>", _token.session());
        dataUrl = dataUrl.replace("<DATE>", dateSelection);
        Request dataRequest = new Request.Builder()
                .url(TARGET_URL + dataUrl)
                .build();

        _client.newCall(dataRequest).enqueue(new CustomCallback()
        {
            @Override
            void onSuccess(Response response, String body)
            {
                _pendingRequest = null;
                extractAppointments(body);
            }
        });
    }

    public static Date parseStineDate(String dateString) throws ParseException
    {
        Date date;
        // "Mai" is not followed by a point which causes parse to fail
        dateString = dateString.replace("Mai", "Mai.");
        date = STINE_DAY_FORMAT.parse(dateString);

        return date;
    }

    private void extractAppointments(String body)
    {
        ArrayList<Appointment> appointments = new ArrayList<>();
        // hold the current date (does not include hour)
        GregorianCalendar calendar = new GregorianCalendar();

        String desc = "";
        String location;
        GregorianCalendar startTime = new GregorianCalendar();
        GregorianCalendar endTime = new GregorianCalendar();

        int paramcount = 0;

        Matcher match = DATA_PATTERN.matcher(body);
        while (match.find())
        {
            String dayString = match.group(2);
            String otherString = match.group(4);

            if (dayString != null)
            {
                try
                {
                    Date date = parseStineDate(dayString);
                    calendar.setTime(date);
                    System.out.println("date parsed: " + dayString);
                } catch (ParseException e)
                {
                    System.out.println("can not parse date: " + dayString);
                }
            }
            if (otherString != null)
            {
                switch (paramcount)
                {
                    case 0: // Name
                        desc = otherString.replace("</a>", "");
                        System.out.println("descr: " + desc);
                        break;
                    case 1: // Prof
                        break;
                    case 2: // startTime - endTime
                        String[] times = otherString.split(" - ");
                        if (times.length == 2)
                        {
                            try
                            {
                                Date start = HOUR_FORMAT.parse(times[0]);
                                startTime.setTime(start);
                                startTime.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
                                startTime.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                                startTime.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                            } catch (ParseException e)
                            {
                                System.out.println("can not parse hour: " + times[0]);
                            }
                            try
                            {
                                Date end = HOUR_FORMAT.parse(times[1]);
                                endTime.setTime(end);
                                endTime.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
                                endTime.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                                endTime.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                            } catch (ParseException e)
                            {
                                System.out.println("can not parse hour: " + times[1]);
                            }
                        }
                        break;
                    case 3: // location
                        location = otherString;
                        System.out.println("location: " + location);
                        appointments.add(new Appointment(startTime.getTime(), endTime.getTime(), location, desc));

                }
                paramcount = (paramcount + 1) % 4;
            }
        }

        _bus.post(new AppointmentsLoadedEvent(appointments));

    }

    abstract class CustomCallback implements Callback
    {
        @Override
        public void onFailure(Request request, IOException e)
        {
            _bus.post(new NetworkFailureEvent(e.toString()));
        }

        @Override
        public void onResponse(Response response)
        {
            System.out.println(response.toString());
            if (!response.isSuccessful())
            {
                _bus.post(new NetworkFailureEvent("unexpected server response " + response.code()));
                return;
            }

            try
            {
                String body = response.body().string();
                if (body.contains("Zugang verweigert") || body.contains("<h1>Timeout!</h1>") || body.contains("verhindern das Speichern von Cookies"))
                {
                    _bus.post(new RequestTokenEvent(_token.toString()));
                }
                else
                {
                    onSuccess(response, body);
                }

            } catch (IOException e)
            {
                onFailure(response.request(), e);
            }
        }

        abstract void onSuccess(Response response, String body);
    }
}
