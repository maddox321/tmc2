package com.maddox.tmc;

/**
 * Code owner Maciej Wawryk
 * Contact: wawryk2@gmail.com
 * Contact: 505525431
 * Created by Maddox on 2015-05-04.
 * In project TMC.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;

import static android.graphics.Paint.*;


public class MapActivity extends ActionBarActivity {

    private MapView mapView;
    private GPSTracker gpsTracker;
//    private MapController mapController;
//    private SimpleLocationOverlay mMyLocationOverlay;
//    private ScaleBarOverlay mScaleBarOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        gpsTracker = new GPSTracker(getBaseContext());

//        new AlertDialog.Builder(this)
//                .setTitle(String.valueOf(gpsTracker.getLongitude()))
//                .setMessage(String.valueOf(gpsTracker.getLatitude()))
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // continue with delete
//                    }
//                })
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // do nothing
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        mapView.getController().setZoom(15);
        mapView.setMaxZoomLevel(18);

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

//        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
//        mapView.setTileSource((new XYTileSource("map", null, 0, 18, 256, "", new String[]{"http://tile.openstreetmap.org/"})));
//        final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getBaseContext());
        mapView.getController().setCenter(new GeoPoint(gpsTracker.getLatitude(),gpsTracker.getLongitude()));

        GeoPoint point = new GeoPoint(gpsTracker.getLatitude(),gpsTracker.getLongitude());

        OverlayItem myLocationOverlayItem = new OverlayItem("Current Position", "Current Position", point);
        Drawable myCurrentLocationMarker = this.getResources().getDrawable(R.drawable.location);
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

//        mapView.getController().setCenter(new GeoPoint(54.51913889, 18.54650541));
//        mapView.getOverlays().add(tilesOverlay);
//        this.mMyLocationOverlay = new SimpleLocationOverlay(this);
//        this.mapView.getOverlays().add(mMyLocationOverlay);
//        this.mScaleBarOverlay = new ScaleBarOverlay(this);
//        this.mapView.getOverlays().add(mScaleBarOverlay);
//        mapView.setUseDataConnection(false);

    }

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_map);
//    }
//
//
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
}