package com.inv3rs3.mobilestine.services;

import com.inv3rs3.mobilestine.data.StineAuthToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * Class for logging in to Stine
 */
public class StineLoginService
{
    private static final String LOGIN_PARAMS = "&APPNAME=CampusNet&PRGNAME=LOGINCHECK&ARGUMENTS=clino%2Cusrname%2Cpass%2Cmenuno%2Cmenu_type%2Cbrowser%2Cplatform&clino=000000000000001&menuno=000265&menu_type=classic&browser=&platform=";
    private static final String COOKIE_URL = "?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N000000000000001,-N000265,-Astartseite";

    private static final MediaType MEDIA_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");
    private static final String SESSION_START = "ARGUMENTS=";

    private static final String COOKIE_IO_ERROR = "Can not get cookie from stine";
    private static final String SESSION_IO_ERROR = "Can not communicate with stine";

    private OkHttpClient _client;
    private CookieManager _cookieManager;

    public StineLoginService()
    {
        _client = new OkHttpClient();
        _cookieManager = new CookieManager();

        _cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        _client.setCookieHandler(_cookieManager);
    }

    /**
     * Login to Stine
     *
     * @param username username
     * @param password password
     * @return StineAuthToken if successful or null if login failed
     * @throws java.io.IOException
     */
    public StineAuthToken login(String username, String password) throws IOException
    {
        Request cookieRequest = new Request.Builder()
                .url(StineService.TARGET_URL + COOKIE_URL)
                .build();
        Response cookieResponse = _client.newCall(cookieRequest).execute();
        if (!cookieResponse.isSuccessful())
        {
            throw new IOException(COOKIE_IO_ERROR);
        }

        RequestBody body = RequestBody.create(MEDIA_URLENCODED,
                "usrname=" + username + "&pass=" + password + LOGIN_PARAMS);

        Request sessionRequest = new Request.Builder()
                .url(StineService.TARGET_URL)
                .post(body)
                .build();

        Response sessionResponse = _client.newCall(sessionRequest).execute();
        if (!sessionResponse.isSuccessful())
        {
            throw new IOException(SESSION_IO_ERROR);
        }

        String session = extractSession(sessionResponse);
        if (session != null)
        {
            return new StineAuthToken(session, _cookieManager.getCookieStore().getCookies());
        }

        return null;
    }

    /**
     * Login to Stine
     *
     * @param username username
     * @param password password
     * @param handler handler
     */
    public void login(final String username, final String password, final LoginHandler handler)
    {
        Request cookieRequest = new Request.Builder()
                .url(StineService.TARGET_URL + COOKIE_URL)
                .build();
        _client.newCall(cookieRequest).enqueue(new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e)
            {
                handler.onError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    RequestBody body = RequestBody.create(MEDIA_URLENCODED,
                            "usrname=" + username + "&pass=" + password + LOGIN_PARAMS);

                    Request sessionRequest = new Request.Builder()
                            .url(StineService.TARGET_URL)
                            .post(body)
                            .build();

                    _client.newCall(sessionRequest).enqueue(new Callback()
                    {
                        @Override
                        public void onFailure(Request request, IOException e)
                        {
                            handler.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException
                        {
                            if (response.isSuccessful())
                            {
                                String session = extractSession(response);
                                if (session != null)
                                {
                                    handler.onSuccess(new StineAuthToken(session, _cookieManager.getCookieStore().getCookies()));
                                } else
                                {
                                    handler.onError(null);
                                }
                            } else
                            {
                                handler.onError(new IOException(SESSION_IO_ERROR));
                            }
                        }
                    });
                } else
                {
                    handler.onError(new IOException(COOKIE_IO_ERROR));
                }
            }
        });
    }

    private String extractSession(Response sessionResponse)
    {
        // a successful login contains a refresh header with session in url
        String refreshParam = sessionResponse.header("REFRESH", "failed");
        if (!refreshParam.equals("failed"))
        {
            int start = refreshParam.indexOf(SESSION_START);
            int end = refreshParam.indexOf(",", start);

            if (start != -1 && end != -1 && start < end)
            {
                // successfully got session
                return refreshParam.substring(start + SESSION_START.length(), end);
            }
        }
        return null;
    }

    public interface LoginHandler
    {
        /**
         * Called if login was successful
         *
         * @param token token to login
         */
        public void onSuccess(StineAuthToken token);

        /**
         * Called if login was not successful
         *
         * @param e null if username or password was wrong
         */
        public void onError(IOException e);
    }
}
