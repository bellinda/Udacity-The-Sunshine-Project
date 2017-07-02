package angelova.gabriela.thesunshineproject.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by W510 on 2.7.2017 г..
 */

//  Make sure you've imported the jobdispatcher.JobService, not job.JobService
//  Add a class called SunshineFirebaseJobService that extends jobdispatcher.JobService
public class SunshineFirebaseJobService extends JobService {

    // Declare an ASyncTask field called mFetchWeatherTask
    private AsyncTask<Void, Void, Void> mFetchWeatherTask;

    //  Override onStartJob and within it, spawn off a separate ASyncTask to sync weather data
    @Override
    public boolean onStartJob(final JobParameters job) {
        mFetchWeatherTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... avoids) {
                Context context = getApplicationContext();
                SunshineSyncTask.syncWeather(context);
                System.out.println("Sync is started");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //  Once the weather data is sync'd, call jobFinished with the appropriate arguements
                jobFinished(job, false);
            }
        };

        mFetchWeatherTask.execute();
        return true;
    }

    // Override onStopJob, cancel the ASyncTask if it's not null and return true
    @Override
    public boolean onStopJob(JobParameters job) {
        if(mFetchWeatherTask != null) {
            mFetchWeatherTask.cancel(true);
        }
        return true;
    }
}
