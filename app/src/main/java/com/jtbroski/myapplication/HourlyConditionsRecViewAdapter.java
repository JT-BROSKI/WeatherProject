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
import java.util.Calendar;

public class HourlyConditionsRecViewAdapter extends RecyclerView.Adapter<HourlyConditionsRecViewAdapter.ViewHolder> {
    private static final String TAG = "HrlyCondRecViewAdapter";

    private final Context context;

    private boolean showPrecipitation;
    private ArrayList<Weather> hourlyWeather = new ArrayList<>();

    public HourlyConditionsRecViewAdapter(Context context) {
        this.context = context;
    }

    public void setShowPrecipitation(boolean showPrecipitation) {
        this.showPrecipitation = showPrecipitation;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_hourly_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");

        Calendar date = hourlyWeather.get(position).getDate();

        // Set week day
        if (Utils.isCurrentDay(date)) {
            holder.txtDateDay.setText(context.getResources().getString(R.string.today_hourly_weather));
        } else {
            String formattedDay = Utils.formatDayHourlyCondition(date);
            holder.txtDateDay.setText(formattedDay);
        }

        // Set day
        String formattedDate = Utils.formatDate(date);
        holder.txtDateNum.setText(formattedDate);

        // Set hour
        String formattedHour = Utils.formatHour(date);
        holder.txtHour.setText(formattedHour);

        // Set icon
        Glide.with(context)
                .asBitmap()
                .load(hourlyWeather.get(position).getIcon())
                .into(holder.imgIcon);

        // Set hourly temperature
        String tempHourly = hourlyWeather.get(position).getTemperatureCurrent() + "\u00B0";
        holder.txtHourlyTemp.setText(tempHourly);

        // Set wind speed, wind scale, and wind direction
        holder.txtWindValue.setText(hourlyWeather.get(position).getWindSpeed());
        holder.txtWindScale.setText(hourlyWeather.get(position).getWindScale());
        holder.txtViewDirection.setText(hourlyWeather.get(position).getWindDirection());

        // Set precipitation chance if applicable
        String precipChance = hourlyWeather.get(position).getPrecipChance();
        if (!precipChance.equals("0")) {
            String precipValue = precipChance + "%";
            holder.txtPrecipChance.setText(precipValue);

            setPrecipitationIcon(holder, position);
            holder.precipIcon.setVisibility(View.VISIBLE);
        } else {
            holder.txtPrecipChance.setText("");
            holder.precipIcon.setVisibility(View.GONE);
        }

        if (showPrecipitation) {
            holder.txtPrecipChance.setVisibility(View.VISIBLE);
        } else {
            holder.txtPrecipChance.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.hourlyWeather.size();
    }

    public void setHourlyWeather(ArrayList<Weather> hourlyWeather) {
        this.hourlyWeather = hourlyWeather;
        notifyDataSetChanged();
    }

    private void setPrecipitationIcon(HourlyConditionsRecViewAdapter.ViewHolder holder, int position) {
        if (hourlyWeather.get(position).getIcon().contains("13")) {
            holder.precipIcon.setImageResource(R.drawable.ic_snowflake);
        } else {
            holder.precipIcon.setImageResource(R.drawable.ic_rain_drop);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtDateDay;
        private final TextView txtDateNum;
        private final TextView txtHour;
        private final ImageView imgIcon;
        private final TextView txtHourlyTemp;
        private final TextView txtWindValue;
        private final TextView txtWindScale;
        private final TextView txtViewDirection;
        private final ImageView precipIcon;
        private final TextView txtPrecipChance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDateDay = itemView.findViewById(R.id.txt_Date_Day);
            txtDateNum = itemView.findViewById(R.id.txt_Date_Num);
            txtHour = itemView.findViewById(R.id.txt_Hour);
            imgIcon = itemView.findViewById(R.id.img_Icon);
            txtHourlyTemp = itemView.findViewById(R.id.txt_Hourly_Temp);
            txtWindValue = itemView.findViewById(R.id.wind_value);
            txtWindScale = itemView.findViewById(R.id.wind_scale);
            txtViewDirection = itemView.findViewById(R.id.wind_direction);
            txtPrecipChance = itemView.findViewById(R.id.precip_chance);
            precipIcon = itemView.findViewById(R.id.precip_icon);
        }
    }
}
