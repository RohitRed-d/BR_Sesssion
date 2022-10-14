package com.gv.csc.helper;

import java.util.HashMap;

public class PLMLOVs {

    private static HashMap<String, HashMap<String, String>> PLM_LOV_LIST =  new HashMap<String, HashMap<String, String>>();

    public static HashMap<String, HashMap<String, String>> getPLM_LOV_LIST() {
        return PLM_LOV_LIST;
    }

    /**
     *
     * @param attKey
     * @param lovKey
     * @param lovValue
     */
    public static void setPLM_LOV_LIST(String attKey, String lovKey, String lovValue) {
        HashMap<String, String> tempLOV;
        if(PLM_LOV_LIST.containsKey(attKey)) {
            tempLOV = PLM_LOV_LIST.get(attKey);
            tempLOV.put(lovKey, lovValue);
            PLM_LOV_LIST.put(attKey, tempLOV);
        } else {
            tempLOV = new HashMap<String, String>();
            tempLOV.put(lovKey, lovValue);
            PLM_LOV_LIST.put(attKey, tempLOV);
        }
    }
}
