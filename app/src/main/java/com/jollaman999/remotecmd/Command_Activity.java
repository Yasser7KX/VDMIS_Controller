package com.jollaman999.remotecmd;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

public class Command_Activity extends AppCompatActivity {

    private DrawerLayout drawer;
    ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;

    Toolbar mtoolbar;

    static Socket socket;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.command_main);

        mContext = getApplicationContext();

        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        mtoolbar.setTitle("Command - Input");
        setSupportActionBar(mtoolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, mtoolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setCheckedItem(R.id.nav_input);
        mNavigationView.setNavigationItemSelectedListener(new NavViewItemListener());

        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_layout, Fragment.instantiate(getApplicationContext(), "com.jollaman999.remotecmd.Command_Input"));
        ft.commit();
    }

    @Override
    public void onDestroy() {
        closeSocket();
        // Should clear schedule items when destroy command activity.
        Schedule_Main.mArrayAdapter.clear();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private class NavViewItemListener implements NavigationView.OnNavigationItemSelectedListener {

        String title;

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            int id = item.getItemId();
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            switch (id) {
                case R.id.nav_input:
                    mNavigationView.setCheckedItem(R.id.nav_input);
                    ft.replace(R.id.fragment_layout, Fragment.instantiate(getApplicationContext(), "com.jollaman999.remotecmd.Command_Input"));
                    ft.commit();
                    title = "Command - Input";
                    break;
                case R.id.nav_shutdown:
                    mNavigationView.setCheckedItem(R.id.nav_shutdown);
                    ft.replace(R.id.fragment_layout, Fragment.instantiate(getApplicationContext(), "com.jollaman999.remotecmd.Command_Shutdown"));
                    ft.commit();
                    title = "Command - Shutdown";
                    break;
                case R.id.nav_schedule:
                    mNavigationView.setCheckedItem(R.id.nav_schedule);
                    ft.replace(R.id.fragment_layout, Fragment.instantiate(getApplicationContext(), "com.jollaman999.remotecmd.Schedule_Main"));
                    ft.commit();
                    title = "Command - Schedule";
                    break;
                case R.id.nav_chat:
                    mNavigationView.setCheckedItem(R.id.nav_chat);
                    ft.replace(R.id.fragment_layout, Fragment.instantiate(getApplicationContext(), "com.jollaman999.remotecmd.Chatting_Login"));
                    ft.commit();
                    title = "Command - Chatting";
                    break;
            }

            drawer.closeDrawer(GravityCompat.START);
            mtoolbar.setTitle(title);

            return true;
        }
    }

    private static void closeSocket() {
        try {
            if (socket != null) {
                socket.close();
            }
            Socket_Control.is_connected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Socket_Control.ResetMain();

        Toast.makeText(RemoteCMD.context,
                RemoteCMD.context.getString(R.string.command_disconnect),
                Toast.LENGTH_SHORT).show();
    }
}
