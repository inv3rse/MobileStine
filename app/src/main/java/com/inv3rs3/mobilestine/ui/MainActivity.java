package com.inv3rs3.mobilestine.ui;

import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.inv3rs3.mobilestine.R;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerCallbacks
{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment _navigationDrawerFragment;
    private Toolbar _toolbar;
    private int _currentFragment;

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

}
