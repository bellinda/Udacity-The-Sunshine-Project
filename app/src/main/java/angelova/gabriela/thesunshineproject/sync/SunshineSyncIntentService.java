package angelova.gabriela.thesunshineproject.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by W510 on 2.7.2017 г..
 */
// Create a new class called SunshineSyncIntentService that extends IntentService
public class SunshineSyncIntentService extends IntentService {

    //  Create a constructor that calls super and passes the name of this class
    public SunshineSyncIntentService() {
        super(SunshineSyncIntentService.class.getName());
    }

    // Override onHandleIntent, and within it, call SunshineSyncTask.syncWeather
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SunshineSyncTask.syncWeather(this);
    }
}
