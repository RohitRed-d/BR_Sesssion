package com.gv.csc.util;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

public class OrderedJSONObject extends JSONObject {
    public OrderedJSONObject() {
        super();
        try {
            Field map = JSONObject.class.getDeclaredField("map");
            map.setAccessible(true);
            Object mapValue = map.get(this);
            if(!(mapValue instanceof LinkedHashMap)) {
                map.set(this, new LinkedHashMap<String, Object>());
            }
        } catch(NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
