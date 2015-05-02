package com.inv3rs3.mobilestine.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.inv3rs3.mobilestine.R;
import com.inv3rs3.mobilestine.application.BusProvider;
import com.inv3rs3.mobilestine.data.StineAuthToken;
import com.inv3rs3.mobilestine.events.RequestTokenEvent;
import com.inv3rs3.mobilestine.events.SetTokenEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerCallbacks, SelectionDialogFragment.SelectionDialogCallback
{
    private static String KEY_SELECTED_FRAGMENT = "SELECTED_FRAGMENT";
    private static String KEY_CURRENT_USER = "CURRENT_USER";
    private static String KEY_ACCOUNT_SELECTION_ACTIVE = "ACCOUNT_SELECTION_ACTIVE";

    private NavigationDrawerFragment _navigationDrawerFragment;
    private Toolbar _toolbar;
    private int _currentFragment;
    private Bus _bus;

    private Account _currentUser;
    private boolean _accountSelectionActive;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(_toolbar);

        _navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);
        _navigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), _toolbar);

        _currentUser = null;
        _accountSelectionActive = false;

        if (savedInstanceState != null)
        {
            _currentFragment = savedInstanceState.getInt(KEY_SELECTED_FRAGMENT);
            _accountSelectionActive = savedInstanceState.getBoolean(KEY_ACCOUNT_SELECTION_ACTIVE);
            String username = savedInstanceState.getString(KEY_CURRENT_USER);

            if (username != null)
            {
                _navigationDrawerFragment.setUserData(username);
                _currentUser = new Account(username, getString(R.string.account_type_mobilestine));
            }
        }
        else
        {
            setFragment(0);
        }

        if (_currentUser == null)
        {
            getAccount();
        }

        _bus = BusProvider.getInstance();
        _bus.register(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        _bus.unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_FRAGMENT, _currentFragment);
        outState.putBoolean(KEY_ACCOUNT_SELECTION_ACTIVE, _accountSelectionActive);
        if (_currentUser != null)
        {
            outState.putString(KEY_CURRENT_USER, _currentUser.name);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        if (_currentFragment != position && position >= 0 && position < 1)
        {
            setFragment(position);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (_navigationDrawerFragment.isDrawerOpen())
            _navigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (!_navigationDrawerFragment.isDrawerOpen())
        {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        if (requestCode == 1337 && resultCode == RESULT_OK)
        {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            _navigationDrawerFragment.setUserData(accountName);
            System.out.println(accountName);
        }
    }

    private void getAccount()
    {
        if (_accountSelectionActive)
        {
            return;
        }

        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(getString(R.string.account_type_mobilestine));

        if (accounts.length == 0)
        {
            addAccount(accountManager);
        }
        else if (accounts.length == 1)
        {
            _currentUser = accounts[0];
            _navigationDrawerFragment.setUserData(_currentUser.name);
        }
        else
        {
            selectAccount(accounts);
        }

    }

    private void addAccount(AccountManager accountManager)
    {
        _accountSelectionActive = true;
        accountManager.addAccount(getString(R.string.account_type_mobilestine), StineAuthToken.AUTH_TOKEN_TYPE, null, null, this, new AccountManagerCallback<Bundle>()
        {
            @Override
            public void run(AccountManagerFuture<Bundle> future)
            {
                _accountSelectionActive = false;
                try
                {
                    Bundle result = future.getResult();
                    String username = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);

                    _currentUser = new Account(username, accountType);
                    _navigationDrawerFragment.setUserData(username);
                } catch (OperationCanceledException | IOException | AuthenticatorException e)
                {
                    e.printStackTrace();
                }
            }
        }, null);
    }

    private void selectAccount(Account[] accounts)
    {
        _accountSelectionActive = true;
        String[] names = new String[accounts.length + 1];
        for (int i = 0; i < accounts.length; i++)
        {
            names[i] = accounts[i].name;
        }

        names[accounts.length] = getString(R.string.account_picker_add_account);

        SelectionDialogFragment dialog = SelectionDialogFragment.create(getString(R.string.dialog_select_account), names);
        dialog.show(getFragmentManager(), "accountSelectionDialog");
    }

    private void getAuthToken()
    {
        if (_currentUser == null)
        {
            getAccount();
            return;
        }

        AccountManager accountManager = AccountManager.get(this);
        accountManager.getAuthToken(_currentUser, StineAuthToken.AUTH_TOKEN_TYPE, new Bundle(), false, new AccountManagerCallback<Bundle>()
        {
            @Override
            public void run(AccountManagerFuture<Bundle> future)
            {
                System.out.println("was here");
                try
                {
                    Bundle result = future.getResult();
                    StineAuthToken token = StineAuthToken.fromString(result.getString(AccountManager.KEY_AUTHTOKEN));
                    _bus.post(new SetTokenEvent(token));

                    System.out.println("got auth token " + token.session());
                } catch (OperationCanceledException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (AuthenticatorException e)
                {
                    e.printStackTrace();
                }
            }
        }, null);
    }

    private void setFragment(int fragmentNumber)
    {
        int fragmentRes;
        switch (fragmentNumber)
        {
            case 0:
                fragmentRes = R.id.fragment_appointments;
                break;
            default:
                System.out.println("fragmentNumber does not exist");
                return;
        }

        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentById(fragmentRes);

        if (fragment == null)
        {
            System.out.println("fragment new created");
            switch (fragmentRes)
            {
                case R.id.fragment_appointments:
                    fragment = new AppointmentsFragment();
                    break;
                default:
                    System.out.println("Fragment does not exist");
                    return;
            }
            manager.beginTransaction().replace(R.id.container, fragment).commit();
        }
        _currentFragment = fragmentNumber;
    }

    @Subscribe
    public void onRequestToken(RequestTokenEvent event)
    {
        if (event.getOldToken() != null)
        {
            AccountManager.get(this).invalidateAuthToken(getString(R.string.account_type_mobilestine), event.getOldToken());
        }
        getAuthToken();
    }

    @Override
    public void selected(String selected, int index)
    {
        if (selected.equals(getString(R.string.account_picker_add_account)))
        {
            addAccount(AccountManager.get(this));
        }
        else
        {
            _accountSelectionActive = false;
            _currentUser = new Account(selected, getString(R.string.account_type_mobilestine));
            _navigationDrawerFragment.setUserData(selected);
        }
    }

    @Override
    public void canceled()
    {
        _accountSelectionActive = false;
    }
}
