package com.example.danstormont.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    // Flag to determine if we want to use a separate view for "today."
    private boolean mUseTodayLayout = true;


    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId;
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
            default: layoutId = -1;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // The ViewHolder contains references to the relevant views, so set the appropriate
        // values through the ViewHolder references instead of costly findViewById calls.
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read weather type from cursor and update view with appropriate image.
        int viewType = getItemViewType(cursor.getPosition());
        if (viewType == VIEW_TYPE_TODAY) {
            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                    cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
        } else {
            viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                    cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
        }

        // Read date from cursor and update view.
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateString));

        // Read weather forecast from cursor and update view.
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(description);

        // For accessibility, add a content description to the icon field.
        viewHolder.iconView.setContentDescription(description);

        // Read user preference for metric or imperial temperature units.
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor and update view.
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String highTempText = Utility.formatTemperature(context, high, isMetric);
        viewHolder.highTempView.setText(highTempText);

        // For accessibility, add "high temp" to the value of the temperature.
        viewHolder.highTempView.setContentDescription("high temperature is " + highTempText);

        // Read low temperature from cursor and update view.
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String lowTempText = Utility.formatTemperature(context, low, isMetric);
        viewHolder.lowTempView.setText(lowTempText);

        // For accessibility, add "low temp" to the value of the temperature.
        viewHolder.lowTempView.setContentDescription("low temperature is " + lowTempText);
    }

    /**
     * An inner class to cache children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

}