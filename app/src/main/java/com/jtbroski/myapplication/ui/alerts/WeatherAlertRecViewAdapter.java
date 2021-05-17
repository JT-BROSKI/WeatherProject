package com.jtbroski.myapplication.ui.alerts;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jtbroski.myapplication.R;
import com.jtbroski.myapplication.WeatherAlert;

import java.util.ArrayList;

public class WeatherAlertRecViewAdapter extends RecyclerView.Adapter<WeatherAlertRecViewAdapter.ViewHolder> {
    private static final String TAG = "AlertRecViewAdapter";

    private final Context context;
    private final ArrayList<WeatherAlert> weatherAlerts;

    public WeatherAlertRecViewAdapter(Context context, ArrayList<WeatherAlert> weatherAlerts) {
        this.context = context;
        this.weatherAlerts = weatherAlerts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_weather_alert, parent, false);
        return new WeatherAlertRecViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");

        try {
            WeatherAlert alert = weatherAlerts.get(position);

            holder.txtAlertTitle.setText(alert.getEvent());
            holder.txtSender.setText(alert.getSender());
            holder.txtFromDate.setText(alert.getStartDate());
            holder.txtToDate.setText(alert.getEndDate());
            holder.txtAlertDescription.setText(alert.getDescription());
        } catch (Exception e) {
            Toast.makeText(context, "Failed to parse weather alert JSON object.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return weatherAlerts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtAlertTitle;
        private final TextView txtSender;
        private final TextView txtFromDate;
        private final TextView txtToDate;
        private final TextView txtAlertDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtAlertTitle = itemView.findViewById(R.id.txt_alert_title);
            txtSender = itemView.findViewById(R.id.txt_sender);
            txtFromDate = itemView.findViewById(R.id.txt_from_date);
            txtToDate = itemView.findViewById(R.id.txt_to_date);
            txtAlertDescription = itemView.findViewById(R.id.txt_alert_description);
        }
    }
}
