package com.maddox.tmc;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

/**
 * Created by bopablo.g on 2015-05-07.
 */
public class MapEventsOverlay extends Overlay {

    private MapEventsReceiver mReceiver;

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean b) {
        //Moze jeszcze bedziemy cos rysowac
    }

    public MapEventsOverlay(Context ctx, MapEventsReceiver receiver) {
        super(ctx);
        mReceiver = receiver;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView){
        Projection proj = mapView.getProjection();
        GeoPoint p = (GeoPoint)proj.fromPixels((int)e.getX(), (int)e.getY());
        return mReceiver.singleTapConfirmedHelper(p);
    }

    @Override
    public boolean onLongPress(MotionEvent e, MapView mapView) {
        Projection proj = mapView.getProjection();
        GeoPoint p = (GeoPoint)proj.fromPixels((int)e.getX(), (int)e.getY());

        return mReceiver.longPressHelper(p);
    }


}
