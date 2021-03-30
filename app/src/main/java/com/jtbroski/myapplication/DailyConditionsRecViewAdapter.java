package com.jtbroski.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class DailyConditionsRecViewAdapter extends RecyclerView.Adapter<DailyConditionsRecViewAdapter.ViewHolder> {
    private static final String TAG = "DailyCondRecViewAdapter";

    private Context context;
    private boolean showPrecipitation;
    private ArrayList<Weather> dailyWeather = new ArrayList<>();

    private int yAxisMax;
    private int yAxisMin;
    private int currentHour;

    private ArrayList<Integer> dailyHighs;
    private ArrayList<Integer> dailyLows;
    private ArrayList<Integer> hoursRecorded;

    private HashMap<Integer, ArrayList> fullDayHourlyConditions;
    private HashMap<Integer, ArrayList> fullDayHourlyRainChance;

    public DailyConditionsRecViewAdapter(Context context) {
        this.context = context;
    }

    public void setShowPrecipitation(boolean showPrecipitation) {
        this.showPrecipitation = showPrecipitation;
    }

    public void sortFullDayHourConditions(JSONArray hourlyConditions, int currentMidnight) {
        dailyHighs = new ArrayList<>();
        dailyLows = new ArrayList<>();
        hoursRecorded = new ArrayList<>();
        fullDayHourlyConditions = new HashMap<>();
        fullDayHourlyRainChance = new HashMap<>();

        currentHour = LocalDateTime.now().getHour();

        // The following booleans are used to determine when to start calculating the highest and lowest temperature of the respective day
        boolean firstDayStart = true;
        boolean secondDayStart = true;
        boolean thirdDayStart = true;

        // These following highest and lowest temperatures are calculated based on the hourly conditions to compare against the daily conditions values
        // because the results from the daily values do not always contain the correct min and max temperatures
        int firstDayHigh = 0;
        int firstDayLow = 0;
        int secondDayHigh = 0;
        int secondDayLow = 0;
        int thirdDayHigh = 0;
        int thirdDayLow = 0;

        int firstDayFirstHour = currentMidnight;
        int firstDayLastHour = firstDayFirstHour + (23 * 3600);
        int secondDayFirstHour = firstDayLastHour + 3600;
        int secondDayLastHour = secondDayFirstHour + (23 * 3600);

        int firstDayIndex = 0;
        int secondDayIndex = 0;
        int thirdDayIndex = 0;
        ArrayList<Entry> firstDayHourlyTemperatures = new ArrayList<>();
        ArrayList<Entry> secondDayHourlyTemperatures = new ArrayList<>();
        ArrayList<Entry> thirdDayHourlyTemperatures = new ArrayList<>();

        ArrayList<Entry> firstDayHourlyRainChance = new ArrayList<>();
        ArrayList<Entry> secondDayHourlyRainChance = new ArrayList<>();
        ArrayList<Entry> thirdDayHourlyRainChance = new ArrayList<>();

        try {
            for (int i = 0; i < hourlyConditions.length(); i++) {
                JSONObject hourlyCondition = hourlyConditions.getJSONObject(i);
                int time = hourlyCondition.getInt("dt");
                int temperature = (int) Math.round(hourlyCondition.getDouble("temp"));

                float rainChance;
                try {
                    rainChance = (float) hourlyCondition.getDouble("pop") * 100;
                } catch (Exception e) {
                    rainChance = 0;
                }

                // Determine the highest and lowest temperatures within the fullDayHourlyConditions
                if (i == 0) {
                    yAxisMax = temperature;
                    yAxisMin = temperature;
                } else if (yAxisMax < temperature) {
                    yAxisMax = temperature;
                } else if (yAxisMin > temperature) {
                    yAxisMin = temperature;
                }

                // Store the hourly temperature to be within its respective 24-hour window
                if (firstDayFirstHour <= time && time <= firstDayLastHour) {
                    if (firstDayStart) {
                        firstDayHigh = temperature;
                        firstDayLow = temperature;
                        firstDayStart = false;
                    } else if (firstDayHigh < temperature) {
                        firstDayHigh = temperature;
                    } else if (firstDayLow > temperature) {
                        firstDayLow = temperature;
                    }

                    if (!hoursRecorded.contains(time)) {
                        firstDayHourlyTemperatures.add(new Entry(firstDayIndex, temperature));
                        firstDayHourlyRainChance.add(new Entry(firstDayIndex, rainChance));
                        firstDayIndex++;
                    }
                } else if (secondDayFirstHour <= time && time <= secondDayLastHour) {
                    if (secondDayStart) {
                        secondDayHigh = temperature;
                        secondDayLow = temperature;
                        secondDayStart = false;
                    } else if (secondDayHigh < temperature) {
                        secondDayHigh = temperature;
                    } else if (secondDayLow > temperature) {
                        secondDayLow = temperature;
                    }

                    if (!hoursRecorded.contains(time)) {
                        secondDayHourlyTemperatures.add(new Entry(secondDayIndex, temperature));
                        secondDayHourlyRainChance.add(new Entry(secondDayIndex, rainChance));
                        secondDayIndex++;
                    }
                } else {
                    if (thirdDayStart) {
                        thirdDayHigh = temperature;
                        thirdDayLow = temperature;
                        thirdDayStart = false;
                    } else if (thirdDayHigh < temperature) {
                        thirdDayHigh = temperature;
                    } else if (thirdDayLow > temperature) {
                        thirdDayLow = temperature;
                    }

                    if (!hoursRecorded.contains(time)) {
                        thirdDayHourlyTemperatures.add(new Entry(thirdDayIndex, temperature));
                        thirdDayHourlyRainChance.add(new Entry(thirdDayIndex, rainChance));
                        thirdDayIndex++;
                    }
                }
                hoursRecorded.add(time);
            }

            // Increase the y-axis maximum value and decrease the y-axis minimum value for chart
            int remainder = yAxisMax % 10;
            yAxisMax += (20 - remainder);
            remainder = yAxisMin % 10;
            yAxisMin -= (10 + remainder);

            dailyHighs.add(firstDayHigh);
            dailyHighs.add(secondDayHigh);
            dailyLows.add(firstDayLow);
            dailyLows.add(secondDayLow);

            fullDayHourlyConditions.put(0, firstDayHourlyTemperatures);
            fullDayHourlyConditions.put(1, secondDayHourlyTemperatures);

            fullDayHourlyRainChance.put(0, firstDayHourlyRainChance);
            fullDayHourlyRainChance.put(1, secondDayHourlyRainChance);

            if (thirdDayHourlyTemperatures.size() != 0) {
                dailyHighs.add(thirdDayHigh);
                dailyLows.add(thirdDayLow);
                fullDayHourlyConditions.put(2, thirdDayHourlyTemperatures);
                fullDayHourlyRainChance.put(2, thirdDayHourlyRainChance);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Failed to sort hourly conditions for DailyConditionsRevViewAdapter.", Toast.LENGTH_SHORT).show();
        }
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

        Calendar date = dailyWeather.get(position).getDate();

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
        // If applicable, check if calculated hourly conditions max/min are available
        String max;
        String min;
        if (position >= 0 && position < fullDayHourlyConditions.size()) {
            max = String.valueOf(dailyHighs.get(position));
            min = String.valueOf(dailyLows.get(position));
        } else {
            max = dailyWeather.get(position).getTemperatureMax();
            min = dailyWeather.get(position).getTemperatureMin();
        }

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
            holder.rainDrop.setVisibility(View.VISIBLE);
        } else {
            holder.txtPrecipChance.setText("");
            holder.rainDrop.setVisibility(View.GONE);
        }

        // Show precipitation value if applicable
        if (showPrecipitation) {
            holder.txtPrecipChance.setVisibility(View.VISIBLE);
        } else {
            holder.txtPrecipChance.setVisibility(View.GONE);
        }

        // Set line chart values if applicable
        configureLineChart(holder);
        addLimitLines(holder, dailyWeather.get(position).getSunrise(), dailyWeather.get(position).getSunset());
        if (position >= 0 && position < fullDayHourlyConditions.size()) {
            holder.lineChart.setData(createLineData(position, max, min));
        } else {
            LineData lineData = new LineData();
            holder.lineChart.setData(lineData);
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

    // Adds limit lines to the x-axis of the line chart to represent the dark hours of the day
    @SuppressLint("ResourceType")
    private void addLimitLines(ViewHolder holder, Calendar sunrise, Calendar sunset) {
        // Set the dark hours color
        int darkHoursColor;
        if (Utils.preferenceDbHelper.getDarkThemeFlag()) {
            darkHoursColor = Color.parseColor(context.getResources().getString(R.color.black));
        } else {
            darkHoursColor = Color.parseColor(context.getResources().getString(R.color.lighter_gray));
        }

        // Create the sunrise limit lines
        int sunriseHour = sunrise.get(Calendar.HOUR_OF_DAY);
        int sunriseMinute = sunrise.get(Calendar.MINUTE);

        // Create the first limit line zone and set its width to the maximum value
        LimitLine lineFirst = new LimitLine(1);
        lineFirst.setLineWidth(12);
        lineFirst.setLineColor(darkHoursColor);
        holder.lineChart.getXAxis().addLimitLine(lineFirst);

        // Create the limit lines between the first line and the actual sunrise line
        for (int i = 2; i < sunriseHour; i++) {
            LimitLine ll = new LimitLine(i);
            ll.setLineWidth(8);
            ll.setLineColor(darkHoursColor);
            holder.lineChart.getXAxis().addLimitLine(ll);
        }

        // Create the actual sunrise limit line
        float sunriseTime = sunriseHour + (sunriseMinute / 60f) - 0.5f;     // the 0.5f value is an estimate correction due to how the line width is expanded
        LimitLine sunriseLine = new LimitLine(sunriseTime);
        sunriseLine.setLineWidth(8);
        sunriseLine.setLineColor(darkHoursColor);
        holder.lineChart.getXAxis().addLimitLine(sunriseLine);

        // Create the sunset limit lines
        int sunsetHour = sunset.get(Calendar.HOUR_OF_DAY);
        int sunsetMinute = sunset.get(Calendar.MINUTE);

        // Create the limit lines between the sunset line and the last line
        for (int i = sunsetHour; i < 22; i++) {
            LimitLine ll = new LimitLine(i);
            ll.setLineWidth(8);
            ll.setLineColor(darkHoursColor);
            holder.lineChart.getXAxis().addLimitLine(ll);
        }

        // Create the last limit line zone and set its width to the maximum value
        LimitLine lineLast = new LimitLine(22);
        lineLast.setLineWidth(12);
        lineLast.setLineColor(darkHoursColor);
        holder.lineChart.getXAxis().addLimitLine(lineLast);

        // Create the actual sunset limit line
        float sunsetTime = sunsetHour + (sunsetMinute / 60f) + 0.5f;    // the 0.5f value is an estimate correction due to how the line width is expanded
        LimitLine sunsetLine = new LimitLine(sunsetTime);
        sunsetLine.setLineWidth(8);
        sunsetLine.setLineColor(darkHoursColor);
        holder.lineChart.getXAxis().addLimitLine(sunsetLine);
    }

    @SuppressLint("ResourceType")
    private void configureLineChart(ViewHolder holder) {
        holder.lineChart.setTouchEnabled(false);
        holder.lineChart.setDragEnabled(false);
        holder.lineChart.setScaleEnabled(false);
        holder.lineChart.setPinchZoom(false);
        holder.lineChart.setDoubleTapToZoomEnabled(false);
        holder.lineChart.setHighlightPerDragEnabled(false);
        holder.lineChart.setHighlightPerTapEnabled(false);
        holder.lineChart.setDescription(null);

        holder.lineChart.getXAxis().setDrawLabels(false);
        holder.lineChart.getXAxis().setDrawGridLines(false);
        holder.lineChart.getXAxis().setAxisMaximum(23);
        holder.lineChart.getXAxis().setDrawLimitLinesBehindData(true);

        holder.lineChart.getAxisRight().setAxisMaximum(100);
        holder.lineChart.getAxisRight().setAxisMinimum(0);
        holder.lineChart.getAxisRight().setDrawGridLines(false);
        holder.lineChart.getAxisRight().setLabelCount(3, true);
        holder.lineChart.getAxisRight().setTextSize(14f);
        holder.lineChart.getAxisRight().setTextColor(Color.parseColor(context.getResources().getString(R.color.rain_blue)));

        holder.lineChart.getAxisLeft().setAxisMaximum(yAxisMax);
        holder.lineChart.getAxisLeft().setAxisMinimum(yAxisMin);
        holder.lineChart.getAxisLeft().setAxisLineWidth(0.75f);
        holder.lineChart.getAxisLeft().setLabelCount(((yAxisMax - yAxisMin) / 10) + 1, true);
        holder.lineChart.getAxisLeft().setTextSize(14f);
        holder.lineChart.getAxisLeft().setTextColor(getThemeTextColor());
        holder.lineChart.getAxisLeft().setGridLineWidth(0.75f);
        holder.lineChart.getAxisLeft().setDrawGridLinesBehindData(false);

        holder.lineChart.getLegend().setEnabled(false);
    }

    private LineData createLineData(int position, String hourlyHigh, String hourlyLow) {
        ArrayList<ILineDataSet> dataSets = createRainChanceLineDataSet(fullDayHourlyRainChance.get(position));
        dataSets.addAll(createTemperatureLineDataSet(position, fullDayHourlyConditions.get(position), hourlyHigh, hourlyLow));

        return new LineData(dataSets);
    }

    @SuppressLint("ResourceType")
    private ArrayList<ILineDataSet> createRainChanceLineDataSet(ArrayList<Entry> entries) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<Entry> rainChanceEntries = new ArrayList<>();
        ArrayList<Entry> zeroChanceEntries = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getY() > 0) {
                if (!(i - 1 < 0) && entries.get(i - 1).getY() == 0) {      // for connecting a non zero entry to a zero entry
                    rainChanceEntries.add(entries.get(i - 1));
                }
                rainChanceEntries.add(entries.get(i));
            } else {
                if (!(i - 1 < 0) && entries.get(i - 1).getY() > 0) {        // for connecting a non zero entry to a zero entry
                    rainChanceEntries.add(entries.get(i));
                }
                zeroChanceEntries.add(entries.get(i));
            }
        }

        LineDataSet rainChanceLineDataSet = new LineDataSet(rainChanceEntries, "");
        rainChanceLineDataSet.setDrawCircles(false);
        rainChanceLineDataSet.setDrawFilled(true);
        rainChanceLineDataSet.setDrawValues(false);
        rainChanceLineDataSet.setLineWidth(4f);
        rainChanceLineDataSet.setColor(Color.parseColor(context.getResources().getString(R.color.rain_blue)));
        rainChanceLineDataSet.setFillDrawable(ContextCompat.getDrawable(context, R.drawable.blue_fill));
        rainChanceLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);     // this supposedly smooths out the line
        rainChanceLineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSets.add(rainChanceLineDataSet);

        LineDataSet zeroChanceLineDataSet = new LineDataSet(zeroChanceEntries, "");
        zeroChanceLineDataSet.setDrawCircles(false);
        zeroChanceLineDataSet.setDrawValues(false);
        zeroChanceLineDataSet.setColor(Color.parseColor(context.getResources().getString(R.color.transparent)));
        zeroChanceLineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSets.add(zeroChanceLineDataSet);

        return dataSets;
    }

    @SuppressLint("ResourceType")
    private ArrayList<ILineDataSet> createTemperatureLineDataSet(int position, ArrayList<Entry> entries, String hourlyHigh, String hourlyLow) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        boolean highFound = false;
        boolean lowFound = false;

        float maxTempurature = Float.parseFloat(hourlyHigh);
        float minTemperature = Float.parseFloat(hourlyLow);

        ArrayList<Entry> nonPeakEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getY() == maxTempurature && !highFound) {
                ArrayList<Entry> highPeakEntry = new ArrayList<>();
                highPeakEntry.add(entries.get(i));

                LineDataSet lineDataSet = new LineDataSet(highPeakEntry, "");
                lineDataSet.setDrawCircles(true);
                lineDataSet.setCircleHoleRadius(3f);
                lineDataSet.setCircleRadius(6f);
                lineDataSet.setDrawValues(false);

                int color = entries.get(i).getX() < currentHour && position == 0 ?
                        Color.parseColor(context.getResources().getString(R.color.light_gray1)) :
                        Color.parseColor(context.getResources().getString(R.color.red));
                lineDataSet.setCircleColor(color);

                dataSets.add(lineDataSet);

                highFound = true;
                nonPeakEntries.add(entries.get(i));
            } else if (entries.get(i).getY() == minTemperature && !lowFound) {
                ArrayList<Entry> lowPeakEntry = new ArrayList<>();
                lowPeakEntry.add(entries.get(i));

                LineDataSet lineDataSet = new LineDataSet(lowPeakEntry, "");
                lineDataSet.setDrawCircles(true);
                lineDataSet.setCircleHoleRadius(3f);
                lineDataSet.setCircleRadius(6f);
                lineDataSet.setDrawValues(false);

                int color = entries.get(i).getX() < currentHour && position == 0 ?
                        Color.parseColor(context.getResources().getString(R.color.light_gray1)) :
                        Color.parseColor(context.getResources().getString(R.color.red));
                lineDataSet.setCircleColor(color);

                dataSets.add(lineDataSet);

                lowFound = true;
                nonPeakEntries.add(entries.get(i));
            } else {
                nonPeakEntries.add(entries.get(i));
            }
        }

        ArrayList<Entry> nonPeakEntriesPast = new ArrayList<>();
        ArrayList<Entry> nonPeakEntriesFuture = new ArrayList<>();
        for (Entry entry : nonPeakEntries) {
            if ((int) entry.getX() < currentHour && position == 0) {
                nonPeakEntriesPast.add(entry);
            } else {
                if ((int) entry.getX() == currentHour) {
                    nonPeakEntriesPast.add(entry);
                }
                nonPeakEntriesFuture.add(entry);
            }
        }

        LineDataSet lineDataSetPast = new LineDataSet(nonPeakEntriesPast, "");
        lineDataSetPast.setDrawCircles(false);
        lineDataSetPast.setDrawValues(false);
        lineDataSetPast.setLineWidth(4f);
        lineDataSetPast.setMode(LineDataSet.Mode.CUBIC_BEZIER);     // this supposedly smooths out the line
        lineDataSetPast.setColor(Color.parseColor(context.getResources().getString(R.color.light_gray1)));
        lineDataSetPast.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSets.add(lineDataSetPast);

        LineDataSet lineDataSetFuture = new LineDataSet(nonPeakEntriesFuture, "");
        lineDataSetFuture.setDrawCircles(false);
        lineDataSetFuture.setDrawValues(false);
        lineDataSetFuture.setLineWidth(4f);
        lineDataSetFuture.setMode(LineDataSet.Mode.CUBIC_BEZIER);   // this supposedly smooths out the line
        lineDataSetFuture.setColor(Color.parseColor(context.getResources().getString(R.color.red)));
        lineDataSetPast.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSets.add(lineDataSetFuture);

        return dataSets;
    }

    private int getThemeTextColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.primaryTextColor, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtDate;
        private ImageView imgIcon;
        private TextView txtTempMaxMin;
        private TextView txtWindValue;
        private TextView txtWindScale;
        private TextView txtViewDirection;
        private TextView txtPrecipChance;
        private ImageView rainDrop;
        private LineChart lineChart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDate = itemView.findViewById(R.id.txt_Date);
            imgIcon = itemView.findViewById(R.id.img_Icon);
            txtTempMaxMin = itemView.findViewById(R.id.txt_Temp_Max_Min);
            txtWindValue = itemView.findViewById(R.id.wind_value);
            txtWindScale = itemView.findViewById(R.id.wind_scale);
            txtViewDirection = itemView.findViewById(R.id.wind_direction);
            txtPrecipChance = itemView.findViewById(R.id.precip_chance);
            rainDrop = itemView.findViewById(R.id.rain_drop);
            lineChart = itemView.findViewById(R.id.line_Chart);
        }
    }
}
