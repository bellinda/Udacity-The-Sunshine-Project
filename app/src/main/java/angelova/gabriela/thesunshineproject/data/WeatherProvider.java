package angelova.gabriela.thesunshineproject.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import angelova.gabriela.thesunshineproject.utilities.SunshineDateUtils;

/**
 * Created by W510 on 4.6.2017 г..
 */

public class WeatherProvider extends ContentProvider {

    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;

    WeatherDbHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(WeatherContract.AUTHORITY, WeatherContract.PATH_WEATHER, CODE_WEATHER);
        uriMatcher.addURI(WeatherContract.AUTHORITY, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    /**
     * Handles requests to insert a set of new rows. In Sunshine, we are only going to be
     * inserting multiple rows of data at a time from a weather forecast. There is no use case
     * for inserting a single row of data into our ContentProvider, and so we are only going to
     * implement bulkInsert. In a normal ContentProvider's implementation, you will probably want
     * to provide proper functionality for the insert method as well.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     *
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_WEATHER:
                db.beginTransaction();
                int insertedRows = 0;
                try {
                    for(ContentValues value : values) {
                        long weatherDate = value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if(!SunshineDateUtils.isDateNormalized(weatherDate)) {
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }
                        long id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if(id != -1) {
                            insertedRows++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if(insertedRows > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return insertedRows;
            default:
                return super.bulkInsert(uri, values);
        }
    }


    /**
     * Handles query requests from clients. We will use this method in Sunshine to query for all
     * of our weather data as well as to query for the weather on a particular day.
     *
     * @param uri           The URI to query
     * @param projection    The list of columns to put into the cursor. If null, all columns are
     *                      included.
     * @param selection     A selection criteria to apply when filtering rows. If null, then all
     *                      rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the
     *                      selection.
     * @param sortOrder     How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query. In our implementation,
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor resultCursor;
        switch (match) {
            case CODE_WEATHER:
                resultCursor = db.query(WeatherContract.WeatherEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_WEATHER_WITH_DATE:
                String normalizedUtcDateString = uri.getLastPathSegment();

                /*
                 * The query method accepts a string array of arguments, as there may be more
                 * than one "?" in the selection statement. Even though in our case, we only have
                 * one "?", we have to create a string array that only contains one element
                 * because this method signature accepts a string array.
                 */
                String[] selectionArguments = new String[]{normalizedUtcDateString};

                resultCursor = mOpenHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        /*
                         * A projection designates the columns we want returned in our Cursor.
                         * Passing null will return all columns of data within the Cursor.
                         * However, if you don't need all the data from the table, it's best
                         * practice to limit the columns returned in the Cursor with a projection.
                         */
                        projection,
                        /*
                         * The URI that matches CODE_WEATHER_WITH_DATE contains a date at the end
                         * of it. We extract that date and use it with these next two lines to
                         * specify the row of weather we want returned in the cursor. We use a
                         * question mark here and then designate selectionArguments as the next
                         * argument for performance reasons. Whatever Strings are contained
                         * within the selectionArguments array will be inserted into the
                         * selection statement by SQLite under the hood.
                         */
                        WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }

        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return resultCursor;
    }

    /**
     * Deletes data at a given URI with optional arguments for more fine tuned deletions.
     *
     * @param uri           The full URI to query
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs Used in conjunction with the selection statement
     * @return The number of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;
        if(selection == null) {
            selection = "1";
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        switch(match) {
            case CODE_WEATHER:
                numRowsDeleted = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    /**
     * In Sunshine, we aren't going to do anything with this method. However, we are required to
     * override it as WeatherProvider extends ContentProvider and getType is an abstract method in
     * ContentProvider. Normally, this method handles requests for the MIME type of the data at the
     * given URI. For example, if your app provided images at a particular URI, then you would
     * return an image URI from this method.
     *
     * @param uri the URI to query.
     * @return nothing in Sunshine, but normally a MIME type string, or null if there is no type.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType in Sunshine.");
    }

    /**
     * In Sunshine, we aren't going to do anything with this method. However, we are required to
     * override it as WeatherProvider extends ContentProvider and insert is an abstract method in
     * ContentProvider. Rather than the single insert method, we are only going to implement
     * {@link WeatherProvider#bulkInsert}.
     *
     * @param uri    The URI of the insertion request. This must not be null.
     * @param values A set of column_name/value pairs to add to the database.
     *               This must not be null
     * @return nothing in Sunshine, but normally the URI for the newly inserted item.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new RuntimeException(
                "We are not implementing insert in Sunshine. Use bulkInsert instead");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("We are not implementing update in Sunshine");
    }

    /**
     * You do not need to call this method. This is a method specifically to assist the testing
     * framework in running smoothly. You can read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}