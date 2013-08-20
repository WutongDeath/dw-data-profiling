package com.anjuke.dw.data_profiling.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.simple.JSONValue;


public final class Functions {

    private Functions() {}

    public static int bitwiseAnd(int a, int b) {
        return a & b;
    }

    public static String joinString(List<String> list, String separator) {

        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(separator);
            }
        }
        return sb.toString();

    }

    private static Pattern ptrnNumericType = Pattern.compile("(?i)(int|decimal|float|double)");
    private static Pattern ptrnStringType = Pattern.compile("(?i)(char|text)");
    private static Pattern ptrnDatetimeType = Pattern.compile("(?i)(date|time)");

    public static int parseTypeFlag(String type) {

        if (ptrnNumericType.matcher(type).find()) {
            return 1;
        } else if (ptrnStringType.matcher(type).find()) {
            return 2;
        } else if (ptrnDatetimeType.matcher(type).find()) {
            return 4;
        } else {
            return 0;
        }

    }

    public static String output(String status, String message) {
        Map<String, String> result = new HashMap<String, String>();
        result.put("status", status);
        if (message != null) {
            result.put("msg", message);
        }
        return JSONValue.toJSONString(result);
    }

    public static String output(String status) {
        return output(status, null);
    }

}
