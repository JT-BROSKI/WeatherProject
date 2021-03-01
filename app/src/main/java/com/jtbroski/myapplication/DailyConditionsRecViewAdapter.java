package com.jtbroski.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Date;

public class DailyConditionsRecViewAdapter extends RecyclerView.Adapter<DailyConditionsRecViewAdapter.ViewHolder> {
    private static final String TAG = "DailyCondRecViewAdapter";

    private Context context;
    private boolean showPrecipitation;
    private ArrayList<Weather> dailyWeather = new ArrayList<>();

    public DailyConditionsRecViewAdapter(Context context) {
        this.context = context;
    }

    public void setShowPrecipitation(boolean showPrecipitation) {
        this.showPrecipitation = showPrecipitation;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_daily_weather, parent, false);
        return new DailyConditionsRecViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");

        Date date = dailyWeather.get(position).getDate();

        // Set date
        if (Utils.isCurrentDay(date)) {
            holder.txtDate.setText("TODAY");
        } else {
            String formattedDay = Utils.formatDayDailyCondition(date);
            holder.txtDate.setText(formattedDay);
        }

        // Set icon
        Glide.with(context)
                .asBitmap()
                .load(dailyWeather.get(position).getIcon())
                .into(holder.imgIcon);

        // Set high and low temperatures
        String max = dailyWeather.get(position).getTemperatureMax();
        String min = dailyWeather.get(position).getTemperatureMin();
        String tempMaxMin = max + "\u00B0" + " | " + min + "\u00B0";
        holder.txtTempMaxMin.setText(tempMaxMin);

        // Set wind speed, wind scale, and wind direction
        holder.txtWindValue.setText(dailyWeather.get(position).getWindSpeed());
        holder.txtWindScale.setText(dailyWeather.get(position).getWindScale());
        holder.txtViewDirection.setText(dailyWeather.get(position).getWindDirection());

        // Set precipitation chance if applicable
        String precipChance = dailyWeather.get(position).getPrecipChance();
        if (!precipChance.equals("0")) {
            String precipValue = precipChance + "%";
            holder.txtPrecipChance.setText(precipValue);
        } else {
            holder.txtPrecipChance.setText("");
        }

        if (showPrecipitation) {
            holder.txtPrecipChance.setVisibility(View.VISIBLE);
        } else {
            holder.txtPrecipChance.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.dailyWeather.size();
    }

    public void setDailyWeather(ArrayList<Weather> dailyWeather) {
        this.dailyWeather = dailyWeather;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtDate;
        private ImageView imgIcon;
        private TextView txtTempMaxMin;
        private TextView txtWindValue;
        private TextView txtWindScale;
        private TextView txtViewDirection;
        private TextView txtPrecipChance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDate = itemView.findViewById(R.id.txt_Date);
            imgIcon = itemView.findViewById(R.id.img_Icon);
            txtTempMaxMin = itemView.findViewById(R.id.txt_Temp_Max_Min);
            txtWindValue = itemView.findViewById(R.id.wind_value);
            txtWindScale = itemView.findViewById(R.id.wind_scale);
            txtViewDirection = itemView.findViewById(R.id.wind_direction);
            txtPrecipChance = itemView.findViewById(R.id.precip_chance);
        }
    }
}
