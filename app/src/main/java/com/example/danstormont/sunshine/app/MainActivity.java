package com.example.danstormont.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by adding or replacing
            // the detail fragment using a fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        ForecastFragment forecastFragment = ((ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // If the settings menu item is selected, launch the settings activity.
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        // If the display location menu item is selected, launch a map.
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Helper method to open location in map using implicit intent.
    private void openPreferredLocationInMap() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPrefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        // Using the URI scheme for showing a location.
        Uri geoLocation = Uri.parse("geo:0,0")
                .buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call" + location + ", no receiving apps found.");
        }
    }

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane) {
            // In two pane mode, show the detail view in this activity by adding or replacing
            // the detail fragment using a fragment transaction.
            Bundle args = new Bundle();
            args.putString(DetailActivity.DATE_KEY, date);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailActivity.DATE_KEY, date);
            startActivity(intent);
        }
    }

    // Events to monitor Android Lifecycle activities.
    @Override
    protected void onStart() {
        super.onStart();

        Log.v(LOG_TAG, "onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(LOG_TAG, "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v(LOG_TAG, "onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.v(LOG_TAG, "onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.v(LOG_TAG, "onDestroy");

    }

}
