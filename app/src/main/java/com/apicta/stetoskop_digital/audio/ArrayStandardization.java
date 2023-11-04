package com.apicta.stetoskop_digital.audio;

import java.util.ArrayList;

public class ArrayStandardization {
    public static ArrayList<Float> standardizationFloatArrayList(ArrayList<Float> floatList) {
        ArrayList<Float> normalizedList = new ArrayList<Float>();
        float minValue = Float.MAX_VALUE;
        float maxValue = -Float.MAX_VALUE;

        // Temukan nilai minimum dan maksimum dalam daftar
        for (float value : floatList) {
            if (value < minValue) {
                minValue = value;
            }
            if (value > maxValue) {
                maxValue = value;
            }
        }

        // Normalisasi nilai dalam daftar dan tambahkan ke normalizedList
        for (float value : floatList) {
            float normalizedValue = (value - minValue) / (maxValue - minValue);
            normalizedList.add(normalizedValue);
        }

        return normalizedList;
    }

}

