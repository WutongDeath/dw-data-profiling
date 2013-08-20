package com.anjuke.dw.data_profiling.util;

import java.util.Iterator;
import java.util.List;


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

}
