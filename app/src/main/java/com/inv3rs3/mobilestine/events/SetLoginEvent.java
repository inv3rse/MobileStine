package com.inv3rs3.mobilestine.events;

public class SetLoginEvent
{
    private String _username;
    private String _password;
    private boolean _save;

    public SetLoginEvent(String username, String password, boolean save)
    {
        _username = username;
        _password = password;
        _save = save;
    }

    public String username()
    {
        return _username;
    }

    public String password()
    {
        return _password;
    }

    public boolean save()
    {
        return _save;
    }
}
