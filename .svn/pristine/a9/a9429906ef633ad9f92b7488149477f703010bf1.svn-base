package com.lk.td.pay.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class JsonUtil {
    /**
     * 把数据源HashMap转换成json
     *
     * @param map
     */
    public static String hashMapToJson(HashMap map) {
        String string = "{";
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            Entry e = (Entry) it.next();
            string += "'" + e.getKey() + "':";
            string += "'" + e.getValue() + "',";
        }
        string = string.substring(0, string.lastIndexOf(","));
        string += "}";
        return string;
    }

    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("username", "xiaoming");
        map.put("password", "123445");
        System.out.println(JsonUtil.hashMapToJson(map));
    }
}
