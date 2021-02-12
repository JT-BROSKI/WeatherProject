package com.jtbroski.myapplication;

import android.os.Build;

public class Utils {

    public static String convertWindDirection(String value)
    {
        int valueInt = Integer.parseInt(value);

        if (valueInt <= 22 || valueInt >= 338)
        {
            return "(N)";
        }
        else if (valueInt > 22 && valueInt < 68)
        {
            return "(NE)";
        }
        else if (valueInt >= 68 && valueInt <= 121)
        {
            return "(E)";
        }
        else if (valueInt > 121 && valueInt < 158)
        {
            return "(SE)";
        }
        else if (valueInt >= 158 && valueInt <= 202)
        {
            return "(S)";
        }
        else if (valueInt > 202 && valueInt < 248)
        {
            return "(SW)";
        }
        else if (valueInt >= 248 && valueInt <= 292)
        {
            return "(W)";
        }
        else
        {
            return "(NW)";
        }
    }

    public static String createWeatherIconUrl(String value)
    {
        final String prefix = "https://openweathermap.org/img/wn/";
        final String suffix = "@2x.png";

        return prefix + value + suffix;
    }

    public static String roundStringNumberValue(String value)
    {
        int roundedValue =  (int)Math.round(Double.parseDouble(value));
        return String.valueOf(roundedValue);
    }


}
