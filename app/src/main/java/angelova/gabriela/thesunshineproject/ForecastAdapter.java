package angelova.gabriela.thesunshineproject;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import angelova.gabriela.thesunshineproject.data.WeatherContract;
import angelova.gabriela.thesunshineproject.utilities.SunshineDateUtils;
import angelova.gabriela.thesunshineproject.utilities.SunshineWeatherUtils;

/**
 * Created by W510 on 20.5.2017 г..
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    final private Context mContext;
    private Cursor mCursor;
    final private ForecastAdapterOnClickHandler mClickHandler;

    public interface ForecastAdapterOnClickHandler {
        public void onClick(long weatherForDay);
    }


    public ForecastAdapter(ForecastAdapterOnClickHandler listener, Context context ) {
        mClickHandler = listener;
        mContext = context;
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.forecast_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        ForecastAdapterViewHolder viewHolder = new ForecastAdapterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        int idIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry._ID);
        int dateIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        int descriptionIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID);
        int highIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int lowIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);

        mCursor.moveToPosition(position); // get to the right location in the cursor

        long dateInMillis = mCursor.getLong(dateIndex);
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        int weatherId = mCursor.getInt(descriptionIndex);
        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        double high = mCursor.getDouble(highIndex);
        double low = mCursor.getDouble(lowIndex);
        String highAndLowTemperature = SunshineWeatherUtils.formatHighLows(mContext, high, low);

        // Replace the single TextView with Views to display all of the weather info

        String weatherForThisDay = String.format("%s - %s - %s", dateString, description, highAndLowTemperature);
        holder.mDateTextView.setText(dateString);
        holder.mWeatherDescriptionTextView.setText(description);
        holder.mMinTempTextView.setText(SunshineWeatherUtils.formatTemperature(mContext, low));
        holder.mMaxTempTextView.setText(SunshineWeatherUtils.formatTemperature(mContext, high));
        int weatherImageId = SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);
        holder.mWeatherIconImageView.setImageResource(weatherImageId);
    }

    @Override
    public int getItemCount() {
        if(mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor cursor) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == cursor) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = cursor; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //  Replace the weatherSummary TextView with individual weather detail TextViews
        final TextView mDateTextView;
        final TextView mWeatherDescriptionTextView;
        final TextView mMinTempTextView;
        final TextView mMaxTempTextView;

        // Add an ImageView for the weather icon
        public ImageView mWeatherIconImageView;

        public ForecastAdapterViewHolder(View itemView) {
            super(itemView);
            // Get references to all new views and delete this line
            mWeatherIconImageView = (ImageView) itemView.findViewById(R.id.weather_icon);
            mDateTextView = (TextView) itemView.findViewById(R.id.date);
            mWeatherDescriptionTextView = (TextView) itemView.findViewById(R.id.weather_description);
            mMaxTempTextView = (TextView) itemView.findViewById(R.id.max_temp);
            mMinTempTextView = (TextView) itemView.findViewById(R.id.min_temp);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }
}
