package com.inv3rs3.mobilestine.data;

import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the session and cookie for Stine
 */
public class StineAuthToken
{
    public static final String AUTH_TOKEN_TYPE = "stine auth token";
    public static final URI COOKIE_URI = URI.create("http://www.stine.uni-hamburg.de");

    private static final String COOKIE_SPLITTER = "\n";
    private static final String VALUE_SPLITTER = ":.:";

    private String _session;
    private List<HttpCookie> _cookies;

    public static StineAuthToken fromString(String token)
    {
        String[] split = token.split(COOKIE_SPLITTER);
        String session = split[0];
        ArrayList<HttpCookie> cookies = new ArrayList<>();

        for (int i = 1; i < split.length; i++)
        {
            String[] values = split[i].split(VALUE_SPLITTER);
            if (values.length == 5)
            {
                HttpCookie cookie = new HttpCookie(values[0], values[1]);
                cookie.setPath(values[2]);
                cookie.setDomain(values[3]);
                cookie.setVersion(Integer.parseInt(values[4]));
                cookies.add(cookie);
            }
        }

        return new StineAuthToken(session, cookies);
    }

    public StineAuthToken(String session, List<HttpCookie> cookies)
    {
        _session = session;
        _cookies = cookies;
    }

    public String session()
    {
        return _session;
    }

    public List<HttpCookie> cookies()
    {
        return _cookies;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(_session);

        for (HttpCookie cookie : cookies())
        {
            builder.append(COOKIE_SPLITTER).append(cookie.getName())
                    .append(VALUE_SPLITTER).append(cookie.getValue())
                    .append(VALUE_SPLITTER).append(cookie.getPath())
                    .append(VALUE_SPLITTER).append(cookie.getDomain())
                    .append(VALUE_SPLITTER).append(cookie.getVersion());
        }

        return builder.toString();
    }
}
