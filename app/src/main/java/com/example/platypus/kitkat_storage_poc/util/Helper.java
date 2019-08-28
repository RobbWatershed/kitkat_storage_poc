package com.example.platypus.kitkat_storage_poc.util;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by avluis on 06/05/2016.
 * Utility class
 */
public final class Helper {

    public static String capitalizeString(String s) {
        if (s == null || s.isEmpty()) return s;
        else if (s.length() == 1) return s.toUpperCase();
        else return s.substring(0, 1).toUpperCase() + s.toLowerCase().substring(1);
    }

    /**
     * Transforms the given int to format with a given length
     * - If the given length is shorter than the actual length of the string, it will be truncated
     * - If the given length is longer than the actual length of the string, it will be left-padded with the character 0
     *
     * @param value  String to transform
     * @param length Target length of the final string
     * @return Reprocessed string of given length, according to rules documented in the method description
     */
    public static String formatIntAsStr(int value, int length) {
        String result = String.valueOf(value);

        if (result.length() > length) {
            result = result.substring(0, length);
        } else if (result.length() < length) {
            result = String.format("%1$" + length + "s", result).replace(' ', '0');
        }

        return result;
    }

    public static String buildListAsString(List<?> list) {
        return buildListAsString(list, "");
    }

    private static String buildListAsString(List<?> list, String valueDelimiter) {

        StringBuilder str = new StringBuilder();
        if (list != null) {
            boolean first = true;
            for (Object o : list) {
                if (!first) str.append(",");
                else first = false;
                str.append(valueDelimiter).append(o.toString().toLowerCase()).append(valueDelimiter);
            }
        }

        return str.toString();
    }

    public static String decode64(String encodedString) {
        // Pure Java
        //byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(encodedString);
        // Android
        byte[] decodedBytes = android.util.Base64.decode(encodedString, android.util.Base64.DEFAULT);
        return new String(decodedBytes);
    }

    public static int dpToPixel(Context context, int dp) {
        float scaleFactor =
                (1.0f / DisplayMetrics.DENSITY_DEFAULT)
                        * context.getResources().getDisplayMetrics().densityDpi;

        return (int) (dp * scaleFactor);
    }


    public static List<Long> getListFromPrimitiveArray(long[] input) {
        List<Long> list = new ArrayList<>(input.length);
        for (long n : input) list.add(n);
        return list;
    }

    public static List<Integer> getListFromPrimitiveArray(int[] input) {
        List<Integer> list = new ArrayList<>(input.length);
        for (int n : input) list.add(n);
        return list;
    }

    public static long[] getPrimitiveLongArrayFromList(List<Long> integers) {
        long[] ret = new long[integers.size()];
        Iterator<Long> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next();
        }
        return ret;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static float coerceIn(float value, float min, float max) {
        if (value < min) return min;
        else if (value > max) return max;
        else return value;
    }
}
