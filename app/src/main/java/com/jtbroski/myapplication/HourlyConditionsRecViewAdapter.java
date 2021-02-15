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

public class HourlyConditionsRecViewAdapter extends RecyclerView.Adapter<HourlyConditionsRecViewAdapter.ViewHolder>{
    private static final String TAG = "HrlyCondRecViewAdapter";

    private Context context;
    private ArrayList<Weather> hourlyWeather = new ArrayList<>();

    public HourlyConditionsRecViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_hourly_weather, parent, false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");

        Date date = hourlyWeather.get(position).getDate();

        // Set week day
        if (Utils.isCurrentDay(date)) {
            holder.txtDateDay.setText("TDY");
        } else {
            String formattedDay = Utils.formatDay(date);
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

        // Set temperature, wind speed, wind scale, and wind direction
        holder.txtHourlyTemp.setText(hourlyWeather.get(position).getTemperatureCurrent() + "\u00B0");
        holder.txtWindValue.setText(hourlyWeather.get(position).getWindSpeed());
        holder.txtWindScale.setText(hourlyWeather.get(position).getWindScale());
        holder.txtViewDirection.setText(hourlyWeather.get(position).getWindDirection());

        // Set precipitation chance if applicable
        String precipChance = hourlyWeather.get(position).getPrecipChance();
        if (!precipChance.equals("0")) {
            holder.txtPrecipChance.setText(precipChance + "%");
        } else {
            holder.txtPrecipChance.setText("");
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtDateDay;
        private TextView txtDateNum;
        private TextView txtHour;
        private ImageView imgIcon;
        private TextView txtHourlyTemp;
        private TextView txtWindValue;
        private TextView txtWindScale;
        private TextView txtViewDirection;
        private TextView txtPrecipChance;

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
        }
    }
}
