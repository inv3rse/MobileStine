package com.inv3rs3.mobilestine.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.inv3rs3.mobilestine.R;
import com.inv3rs3.mobilestine.data.StineAuthToken;
import com.inv3rs3.mobilestine.services.StineLoginService;

import java.io.IOException;

public class StineAuthenticatorActivity extends AccountAuthenticatorActivity
{
    private StineLoginService _stineLoginService;
    private Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _context = this;
        _stineLoginService = new StineLoginService();

        setContentView(R.layout.activity_stine_authenticator);

        setupHandlers();
    }

    private void setupHandlers()
    {
        Button cancelBtn = (Button) findViewById(R.id.authenticator_cancel);
        Button signInBtn = (Button) findViewById(R.id.authenticator_signin);

        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EditText eUsername = (EditText) findViewById(R.id.authenticator_username);
                EditText ePassword = (EditText) findViewById(R.id.authenticator_password);

                final String username = eUsername.getText().toString();
                final String password = ePassword.getText().toString();

                Toast toast = Toast.makeText(_context, getString(R.string.msg_loggingIn), Toast.LENGTH_SHORT);
                toast.show();

                _stineLoginService.login(username, password, new StineLoginService.LoginHandler()
                {
                    @Override
                    public void onSuccess(StineAuthToken token)
                    {
                        runOnUiThread(new MakeToast(getString(R.string.msg_loginSuccess)));

                        AccountManager accountManager = AccountManager.get(_context);

                        String accountType = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                        Account account = new Account(username, accountType);
                        accountManager.addAccountExplicitly(account, password, null);

                        final Intent result = new Intent();
                        result.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
                        result.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);

                        setAccountAuthenticatorResult(result.getExtras());

                        setResult(RESULT_OK, result);
                        finish();
                    }

                    @Override
                    public void onError(IOException e)
                    {
                        if (e != null)
                        {
                            runOnUiThread(new MakeToast(getString(R.string.msg_networkError)));
                        }
                        else
                        {
                            runOnUiThread(new MakeToast(getString(R.string.msg_loginFailed)));
                        }
                    }
                });
            }
        });
    }

    private class MakeToast implements Runnable
    {
        private String _msg;

        public MakeToast(String msg)
        {
            _msg = msg;
        }

        @Override
        public void run()
        {
            Toast toast = Toast.makeText(_context, _msg, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}
