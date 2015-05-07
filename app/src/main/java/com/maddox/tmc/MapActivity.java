package com.maddox.tmc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;



public class MapActivity extends Activity implements MapEventsReceiver {

    private MapView mapView;
    private GPSTracker gpsTracker;

    private GeoPoint startPoint;
    private GeoPoint currentPoint;

    private int lastIndexOfStart = -1;



   // ArrayList<OverlayItem> overlayItemArray;

    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        addListenerOnButton();


        gpsTracker = new GPSTracker(getBaseContext());

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        mapView.getController().setZoom(15);
        mapView.setMaxZoomLevel(18);

        //Add Scale Bar
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        mapView.getOverlays().add(myScaleBarOverlay);

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setCenter(new GeoPoint(gpsTracker.getLatitude(),gpsTracker.getLongitude()));

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        mapView.getOverlays().add(0, mapEventsOverlay);

    }

    private void addListenerOnButton() {

        button = (Button) findViewById(R.id.button1);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Log.d("onClick", "buttonPressed");


                currentPoint = new GeoPoint(gpsTracker.getLatitude(),gpsTracker.getLongitude());

                OverlayItem myLocationOverlayItem = new OverlayItem("Current Position", "Current Position", currentPoint);
                Drawable myCurrentLocationMarker = mapView.getResources().getDrawable(R.drawable.location);

                myLocationOverlayItem.setMarker(myCurrentLocationMarker);

                final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                items.add(myLocationOverlayItem);

                ItemizedIconOverlay<OverlayItem> currentLocationOverlay;
                DefaultResourceProxyImpl resourceProxy;

                resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

                currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                        new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                return true;
                            }

                            public boolean onItemLongPress(final int index, final OverlayItem item) {
                                return true;
                            }
                        }, resourceProxy);



                mapView.getOverlays().add(currentLocationOverlay);

                mapView.invalidate();
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        //Toast.makeText(this, "Tap on (" + p.getLatitude() + "," + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();
        Log.d("tap","tap");

       //mapView.getOverlays().clear();
         mapView.invalidate();


        startPoint = p;
        OverlayItem startItem = new OverlayItem("Start", "Start", p);
        Drawable myStartItem = mapView.getResources().getDrawable(R.drawable.location);

        startItem.setMarker(myStartItem);

        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(startItem);

        ItemizedIconOverlay<OverlayItem> startOverlay;
        DefaultResourceProxyImpl resourceProxy;

        resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

        startOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, resourceProxy);

        mapView.invalidate();
        if (lastIndexOfStart != -1) {
            mapView.getOverlayManager().remove(lastIndexOfStart);
        }

        mapView.getOverlays().add(startOverlay);

      //  int lastIndexOfStart = mapView.getOverlayManager().;
        Log.d("last:", String.valueOf(lastIndexOfStart));


        mapView.invalidate();

        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        //Toast.makeText(this, "Long Press", Toast.LENGTH_SHORT).show();
        Log.d("press","press");
        return true;
    }
}
