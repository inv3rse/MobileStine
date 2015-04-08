package com.inv3rs3.mobilestine.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.inv3rs3.mobilestine.R;
import com.inv3rs3.mobilestine.application.BusProvider;
import com.inv3rs3.mobilestine.events.RequestLoginEvent;
import com.inv3rs3.mobilestine.events.SetLoginEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerCallbacks
{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment _navigationDrawerFragment;
    private Toolbar _toolbar;
    private int _currentFragment;
    private Bus _bus;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _currentFragment = -1;
        setContentView(R.layout.activity_main);
        _toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(_toolbar);

        _navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        _navigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), _toolbar);
        // populate the navigation drawer
        _navigationDrawerFragment.setUserData("John Doe", "johndoe@doe.com", BitmapFactory.decodeResource(getResources(), R.drawable.avatar));

        _bus = BusProvider.getInstance();
        _bus.register(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        // update the main content by replacing fragments
        Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();

        if (_currentFragment != position && position >= 0 && position < 1)
        {
            if (position == 0)
            {
                AppointmentsFragment fragment = new AppointmentsFragment();
                setFragment(fragment, _currentFragment != -1);
            }
            _currentFragment = position;
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

    private void setFragment(Fragment fragment, boolean replace)
    {
        if (replace)
        {
            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
        else
        {
            getFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }
    }

    @Subscribe
    public void onRequestLogin(RequestLoginEvent event)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_login, null))
                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface anInterface, int id)
                    {
                        AlertDialog dialog = (AlertDialog) anInterface;
                        String username = ((EditText) dialog.findViewById(R.id.username)).getText().toString();
                        String password = ((EditText) dialog.findViewById(R.id.password)).getText().toString();
                        _bus.post(new SetLoginEvent(username, password, true));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface anInterface, int id)
                    {
                        System.out.println("negative");
                    }
                });

        builder.create().show();
    }

}
