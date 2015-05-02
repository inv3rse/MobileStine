package com.inv3rs3.mobilestine.data;


import junit.framework.TestCase;

import java.net.HttpCookie;
import java.util.ArrayList;

public class StineAuthTokenTest extends TestCase
{
    private String _session = "someStringWith12345Numbers?";
    private ArrayList<HttpCookie> _cookies;

    @Override
    protected void setUp()
    {
        _cookies = new ArrayList<>();
        _cookies.add(new HttpCookie("myName", "myValue"));
        _cookies.add(new HttpCookie("my222Name222", "222myValue?"));
        _cookies.add(new HttpCookie("OtherName", "897453793862q58?%7%"));
    }

    public void testStringConversion()
    {
        StineAuthToken token = new StineAuthToken(_session, _cookies);

        String s = token.toString();

        StineAuthToken restored = StineAuthToken.fromString(s);

        assertEquals(token.session(), restored.session());
        assertEquals(token.cookies().size(), restored.cookies().size());

        for (int i = 0; i < token.cookies().size(); i++)
        {
            assertEquals(token.cookies().get(i).getName(), restored.cookies().get(i).getName());
            assertEquals(token.cookies().get(i).getValue(), restored.cookies().get(i).getValue());
        }
    }
}
