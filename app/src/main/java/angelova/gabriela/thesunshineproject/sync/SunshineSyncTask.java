package angelova.gabriela.thesunshineproject.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

import angelova.gabriela.thesunshineproject.data.SunshinePreferences;
import angelova.gabriela.thesunshineproject.data.WeatherContract;
import angelova.gabriela.thesunshineproject.utilities.NetworkUtils;
import angelova.gabriela.thesunshineproject.utilities.NotificationUtils;
import angelova.gabriela.thesunshineproject.utilities.OpenWeatherJsonUtils;

/**
 * Created by W510 on 2.7.2017 г..
 */

// Create a class called SunshineSyncTask
public class SunshineSyncTask {

// Within SunshineSyncTask, create a synchronized public static void method called syncWeather
    synchronized public static void syncWeather(Context context) {
        //  Within syncWeather, fetch new weather data
        try {
        /*
             * The getUrl method will return the URL that we need to get the forecast JSON for the
             * weather. It will decide whether to create a URL based off of the latitude and
             * longitude or off of a simple location as a String.
             */
            URL weatherRequestUrl = NetworkUtils.getUrl(context);

            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);
            /* Parse the JSON into a list of weather values */
            ContentValues[] weatherValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, jsonWeatherResponse);
            // If we have valid results, delete the old data and insert the new
            /*
             * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if(weatherValues != null && weatherValues.length > 0) {
                 /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver sunshineContentResolver = context.getContentResolver();

                /* Delete old weather data because we don't need to keep multiple days' data */
                sunshineContentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI, null, null);

                 /* Insert our new weather data into Sunshine's ContentProvider */
                 sunshineContentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);
            }
            // Check if notifications are enabled
            boolean notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context);


//          Check if a day has passed since the last notification
            long timeSinceLastNotification = SunshinePreferences
                    .getEllapsedTimeSinceLastNotification(context);

            boolean oneDayPassedSinceLastNotification = false;

            if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                oneDayPassedSinceLastNotification = true;
            }

//          If more than a day have passed and notifications are enabled, notify the user
            if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                NotificationUtils.notifyUserOfNewWeather(context);
            }

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }

    }


}
