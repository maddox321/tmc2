package com.maddox.tmc;

import org.osmdroid.util.GeoPoint;

/**
 * Created by bopablo.g on 2015-05-07.
 */
public interface MapEventsReceiver {

    boolean singleTapConfirmedHelper(GeoPoint p);

    boolean longPressHelper(GeoPoint p);
}
