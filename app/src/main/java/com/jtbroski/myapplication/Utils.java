package com.jtbroski.myapplication;

public class Utils {

    public static String roundStringNumberValue(String value)
    {
        int roundedValue =  (int)Math.round(Double.parseDouble(value));
        return String.valueOf(roundedValue);
    }
}
