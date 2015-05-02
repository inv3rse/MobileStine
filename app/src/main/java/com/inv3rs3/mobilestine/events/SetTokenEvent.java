package com.inv3rs3.mobilestine.events;

import com.inv3rs3.mobilestine.data.StineAuthToken;

public class SetTokenEvent
{
    private StineAuthToken _token;

    public SetTokenEvent(StineAuthToken token)
    {
        _token = token;
    }

    public StineAuthToken token()
    {
        return _token;
    }
}
