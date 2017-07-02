package angelova.gabriela.thesunshineproject;

import android.content.Intent;
import android.database.Cursor;
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
import angelova.gabriela.thesunshineproject.utilities.SunshineDateUtils;
import angelova.gabriela.thesunshineproject.utilities.SunshineWeatherUtils;

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

    private TextView mTvDate;
    private TextView mTvDescription;
    private TextView mTvHighTemp;
    private TextView mTvLowTemp;
    private TextView mTvHumidity;
    private TextView mTvWind;
    private TextView mTvPressure;

    private static final String WEATHER_DATA_EXTRA = "weatherForToday";
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String mForecastSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTvDate = (TextView)findViewById(R.id.tv_date);
        mTvDescription = (TextView)findViewById(R.id.tv_description);
        mTvHighTemp = (TextView) findViewById(R.id.tv_high_temperature);
        mTvLowTemp = (TextView) findViewById(R.id.tv_low_temperature);
        mTvHumidity = (TextView) findViewById(R.id.tv_humidity);
        mTvPressure = (TextView) findViewById(R.id.tv_presure);
        mTvWind = (TextView) findViewById(R.id.tv_wind);

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
        mTvDate.setText(dateText);

        int localWeatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        String condition = SunshineWeatherUtils.getStringForWeatherCondition(this, localWeatherId);
        mTvDescription.setText(condition);

        double highTemp = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        String highString = SunshineWeatherUtils.formatTemperature(this, highTemp);
        mTvHighTemp.setText(highString);

        double lowTemp = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        String lowString = SunshineWeatherUtils.formatTemperature(this, lowTemp);
        mTvLowTemp.setText(lowString);

        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        mTvHumidity.setText(humidityString);

        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        mTvWind.setText(windString);

        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);
        String pressureString = getString(R.string.format_pressure, pressure);
        mTvPressure.setText(pressureString);

        mForecastSummary = String.format("%s - %s - %s/%s", dateText, condition, highString, lowString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
