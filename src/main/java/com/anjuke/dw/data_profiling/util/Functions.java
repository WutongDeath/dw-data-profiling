package com.anjuke.dw.data_profiling.util;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


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

}
