package com.inv3rs3.mobilestine.application;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.inv3rs3.mobilestine.data.StineAuthToken;
import com.inv3rs3.mobilestine.services.StineLoginService;
import com.inv3rs3.mobilestine.ui.StineAuthenticatorActivity;

import java.io.IOException;

public class StineAuthenticator extends AbstractAccountAuthenticator
{
    private static final int AUTH_ERROR_CODE = 500;
    private static final String AUTH_ERROR_MSG = "Login failed";

    private Context _context;
    private StineLoginService _stineLoginService;

    public StineAuthenticator(Context context)
    {
        super(context);
        _context = context;
        _stineLoginService = new StineLoginService();
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String s)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException
    {
        final Bundle result = new Bundle();
        final Intent intent;

        intent = new Intent(_context, StineAuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        result.putParcelable(AccountManager.KEY_INTENT, intent);

        return result;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle bundle) throws NetworkErrorException
    {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException
    {
        AccountManager accountManager= AccountManager.get(_context);
        String password = accountManager.getPassword(account);

        StineAuthToken token = null;
        if (password != null)
        {
            try
            {
                token = _stineLoginService.login(account.name, password);
            } catch (IOException e)
            {
                throw new NetworkErrorException("can not communicate with stine");
            }
        }

        final Bundle result = new Bundle();

        if (token != null)
        {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, token.toString());
            System.out.println("authenticator: got token");
        }
        else
        {
            System.out.println("had auth error");
            result.putInt(AccountManager.KEY_ERROR_CODE, AUTH_ERROR_CODE);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, AUTH_ERROR_MSG);
        }

        return result;
    }

    @Override
    public String getAuthTokenLabel(String s)
    {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String s, Bundle bundle) throws NetworkErrorException
    {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] strings) throws NetworkErrorException
    {
        return null;
    }
}
