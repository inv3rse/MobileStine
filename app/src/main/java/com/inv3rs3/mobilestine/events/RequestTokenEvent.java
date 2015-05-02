package com.inv3rs3.mobilestine.events;

public class RequestTokenEvent
{
    private String _oldToken;

    public RequestTokenEvent()
    {
        _oldToken = null;
    }

    public RequestTokenEvent(String oldToken)
    {
        _oldToken = oldToken;
    }

    /**
     * Returns the old token which should be invalidated
     * @return token or null if there is none
     */
    public String getOldToken()
    {
        return _oldToken;
    }
}
