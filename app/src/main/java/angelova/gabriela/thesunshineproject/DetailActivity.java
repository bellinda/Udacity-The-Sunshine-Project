package angelova.gabriela.thesunshineproject;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import angelova.gabriela.thesunshineproject.data.WeatherContract;
import angelova.gabriela.thesunshineproject.databinding.ActivityDetailBinding;
import angelova.gabriela.thesunshineproject.utilities.SunshineDateUtils;
import angelova.gabriela.thesunshineproject.utilities.SunshineWeatherUtils;

import static android.R.attr.description;

/**
 * Created by W510 on 21.5.2017 г..
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;

    private static final int ID_DETAIL_LOADER = 777;

    private Uri mUri;

    //  Declare an ActivityDetailBinding field called mDetailBinding
    ActivityDetailBinding mDetailBinding;

    private static final String WEATHER_DATA_EXTRA = "weatherForToday";
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String mForecastSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Instantiate mDetailBinding using DataBindingUtil
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mUri = getIntent().getData();
        if(mUri == null) {
            throw new NullPointerException("The data is null");
        }

        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_share) {
            shareForecast();
            return true;
        } else if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(DetailActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void shareForecast() {
//        String mimeType = "plain/text";
//        String title = "Share details";
//        ShareCompat.IntentBuilder.from(this)
//                .setType(mimeType)
//                .setChooserTitle(title)
//                .setText(mForecastSummary)
//                .startChooser();
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        startActivity(shareIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        switch(id) {
            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            return;
        }

        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);
        //  Use mDetailBinding to display the date
        mDetailBinding.primaryWeatherInfo.date.setText(dateText);

        int localWeatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        // Display the weather icon using mDetailBinding
        int weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(localWeatherId);
        mDetailBinding.primaryWeatherInfo.weatherIcon.setImageResource(weatherImageId);

        String condition = SunshineWeatherUtils.getStringForWeatherCondition(this, localWeatherId);
        // Create the content description for the description for a11y
        String descriptionA11y = getString(R.string.a11y_forecast, condition);

        // Use mDetailBinding to display the description and set the content description
        mDetailBinding.primaryWeatherInfo.weatherDescription.setText(condition);

        // Set the content description of the icon to the same as the weather description a11y text
        mDetailBinding.primaryWeatherInfo.weatherIcon.setContentDescription(descriptionA11y);

        double highTemp = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        String highString = SunshineWeatherUtils.formatTemperature(this, highTemp);
        // Create the content description for the high temperature for a11y
        String highTempA11y = getString(R.string.a11y_high_temp, highString);

        // Use mDetailBinding to display the high temperature and set the content description
        mDetailBinding.primaryWeatherInfo.maxTemp.setText(highString);

        double lowTemp = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        String lowString = SunshineWeatherUtils.formatTemperature(this, lowTemp);
        // Create the content description for the low temperature for a11y
        String lowTempA11y = getString(R.string.a11y_low_temp, lowString);

        //  Use mDetailBinding to display the low temperature and set the content description
        mDetailBinding.primaryWeatherInfo.minTemp.setText(lowString);

        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        // Create the content description for the humidity for a11y
        String humidityA11y = getString(R.string.a11y_humidity, humidityString);

        //  Use mDetailBinding to display the humidity and set the content description
        mDetailBinding.extraWeatherDetail.humidity.setText(humidityString);
        // Set the content description of the humidity label to the humidity a11y String
        mDetailBinding.extraWeatherDetail.humidity.setContentDescription(humidityA11y);

        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        // Create the content description for the wind for a11y
        String windA11y = getString(R.string.a11y_wind, windString);

        //  Use mDetailBinding to display the wind and set the content description
        mDetailBinding.extraWeatherDetail.wind.setText(windString);
        // Set the content description of the wind label to the wind a11y String
        mDetailBinding.extraWeatherDetail.wind.setContentDescription(windA11y);

        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);
        String pressureString = getString(R.string.format_pressure, pressure);
        // Create the content description for the pressure for a11y
        String pressureA11y = getString(R.string.a11y_pressure, pressureString);

        // Use mDetailBinding to display the pressure and set the content description
        mDetailBinding.extraWeatherDetail.pressure.setText(pressureString);
        // Set the content description of the pressure label to the pressure a11y String
        mDetailBinding.extraWeatherDetail.pressure.setContentDescription(pressureA11y);

        mForecastSummary = String.format("%s - %s - %s/%s", dateText, condition, highString, lowString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
