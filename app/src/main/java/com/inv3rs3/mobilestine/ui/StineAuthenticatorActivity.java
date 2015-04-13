package com.inv3rs3.mobilestine.ui;

import android.accounts.AccountAuthenticatorActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.inv3rs3.mobilestine.R;

public class StineAuthenticatorActivity extends AccountAuthenticatorActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stine_authenticator);
    }

}
