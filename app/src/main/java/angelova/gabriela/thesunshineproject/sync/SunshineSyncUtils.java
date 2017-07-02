package angelova.gabriela.thesunshineproject.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import angelova.gabriela.thesunshineproject.data.WeatherContract;

/**
 * Created by W510 on 2.7.2017 г..
 */

// Create a class called SunshineSyncUtils
public class SunshineSyncUtils {

    // Add constant values to sync Sunshine every 3 - 4 hours
    private static final int SYNC_INTERVAL_SECONDS = 20;
    private static final int SYNC_FLEXTIME_SECONDS = 20;
    // Add a sync tag to identify our sync job
    private static final String SYNC_JOB_TAG = "hydration_reminder_tag";

    //  Declare a private static boolean field called sInitialized
    private static boolean sInitialized;



//  Create a method to schedule our periodic weather sync
    static void scheduleSyncing(@NonNull final Context context) {
        // Create a new GooglePlayDriver
        Driver driver = new GooglePlayDriver(context);
        // Create a new FirebaseJobDispatcher with the driver
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(SunshineFirebaseJobService.class)
                .setTag(SYNC_JOB_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_SECONDS, SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();
        // Use dispatcher's schedule method to schedule the job
        dispatcher.schedule(constraintReminderJob);
    }

    //  Create a synchronized public static void method called initialize
    synchronized public static void initialize(@NonNull final Context context) {
        // Only execute this method body if sInitialized is false
        if(!sInitialized) {
            //  If the method body is executed, set sInitialized to true
            sInitialized = true;

            // Call the method you created to schedule a periodic weather sync
            scheduleSyncing(context);

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    /* URI for every row of weather data in our weather table*/
                    Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;

                     /*
                 * Since this query is going to be used only as a check to see if we have any
                 * data (rather than to display data), we just need to PROJECT the ID of each
                 * row. In our queries where we display data, we need to PROJECT more columns
                 * to determine what weather details need to be displayed.
                 */
                    String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                    String selectionStatement = WeatherContract.WeatherEntry
                            .getSqlSelectForTodayOnwards();

                /* Here, we perform the query to check to see if we have any weather data */
                    Cursor cursor = context.getContentResolver().query(
                            forecastQueryUri,
                            projectionColumns,
                            selectionStatement,
                            null,
                            null);
                /*
                 * A Cursor object can be null for various different reasons. A few are
                 * listed below.
                 *
                 *   1) Invalid URI
                 *   2) A certain ContentProvider's query method returns null
                 *   3) A RemoteException was thrown.
                 *
                 * Bottom line, it is generally a good idea to check if a Cursor returned
                 * from a ContentResolver is null.
                 *
                 * If the Cursor was null OR if it was empty, we need to sync immediately to
                 * be able to display data to the user.
                 */
                    //  If it is empty or we have a null Cursor, sync the weather now!
                    if (null == cursor || cursor.getCount() == 0) {
                        startImmediateSync(context);
                    }

                /* Make sure to close the Cursor to avoid memory leaks! */
                    cursor.close();
                    return null;
                }
            }.execute();
        }
    }


    // Create a public static void method called startImmediateSync
    public static void startImmediateSync(@NonNull final Context context) {

        // Within that method, start the SunshineSyncIntentService
        Intent startSyncing = new Intent(context, SunshineSyncIntentService.class);
        context.startService(startSyncing);
    }
}
