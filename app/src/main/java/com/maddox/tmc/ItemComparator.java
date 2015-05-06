package com.maddox.tmc;

import java.util.Comparator;

import mapData.ElementMapy;

/**
 * Created by bopablo.g on 2015-05-06.
 */
public class ItemComparator implements Comparator {


    public int compare(Object arg0, Object arg1) {

        ElementMapy item1 = (ElementMapy)arg0;
        ElementMapy item2 = (ElementMapy)arg1;

        int type1 = item1.type;
        int type2 = item2.type;

        if(type1 > type2) {
            return 1;
        } else if(type1 == type2) {
            return 0;
        } else {
            return -1;
        }
    }

}
