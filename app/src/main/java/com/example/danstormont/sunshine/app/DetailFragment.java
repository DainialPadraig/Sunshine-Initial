package com.example.danstormont.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.danstormont.sunshine.app.data.WeatherContract;
import com.example.danstormont.sunshine.app.data.WeatherContract.WeatherEntry;

/**
 * The fragment for the detailed forecast view.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final String LOCATION_KEY = "location";


    private ShareActionProvider mShareActionProvider;
    private String mLocation;
    private String mForecast = null;
    private String mDateStr;

    private static final int DETAIL_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with weather
            // data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // Variables to maintain references to the view items.
    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LOCATION_KEY, mLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mDateStr = arguments.getString(DetailActivity.DATE_KEY);
        }

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon_imageview);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle arguments = getArguments();
        if (arguments != null &&
                arguments.containsKey(DetailActivity.DATE_KEY) &&
                mLocation != null &&
                !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar, if present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item.
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoad happens before this, we can go ahead and set the share intent now.
        if (mForecast != null) {
            // TODO - Share intent crashing app when detail forecast selected.
                mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Sort order: Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry
                .buildWeatherLocationWithDate(mLocation, mDateStr);

        // Now create and return a CursorLoader that will take care of creating a cursor
        // for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            // Get shared context so we don't have to do it repeatedly throughout the method.
            Context context = getActivity();

            // Read weather condition ID from cursor
            int weatherId = data.getInt(data.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
            int artResource = Utility.getArtResourceForWeatherCondition(weatherId);
            mIconView.setImageResource(artResource);

            // Read date from cursor and update views for day of week and date.
            String date = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT));
            String friendlyDateText = Utility.getDayName(context, date);
            String dateText = Utility.getFormattedMonthDay(context, date);
            mFriendlyDateView.setText(friendlyDateText);
            mDateView.setText(dateText);

            // Read description from cursor and update view.
            String description = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            mDescriptionView.setText(description);

            // For accessibility, add the description to the icon view.
            mIconView.setContentDescription(description);

            // Get the temperature format.
            boolean isMetric = Utility.isMetric(context);

            // Read high temperature from cursor and update view.
            double high = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
            String highString = Utility.formatTemperature(context, high, isMetric);
            mHighTempView.setText(highString);

            // For accessibility, add "High temp" to temperature value.
            mHighTempView.setContentDescription("high temperature is " + highString);

            // Read low temperature from cursor and update view.
            double low = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
            String lowString = Utility.formatTemperature(context, low, isMetric);
            mLowTempView.setText(lowString);

            // For accessibility, add "low temp" to temperature value.
            mLowTempView.setContentDescription("low temperature is " + lowString);

            // Read the humidity from cursor and update view.
            double humidity = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY));
            mHumidityView.setText(context.getString(R.string.format_humidity, humidity));

            // Read wind speed and direction from cursor and update view.
            double windSpeed = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED));
            double windDir = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_DEGREES));
            mWindView.setText(Utility.getFormattedWind(context, windSpeed, windDir));

            // Read pressure from cursor and update view.
            double pressure = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_PRESSURE));
            mPressureView.setText(context.getString(R.string.format_pressure, pressure));

            // We still need this for the share intent.
            mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}
