package com.sambusgeospatial.featurelayerupdater.ui.home;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.sambusgeospatial.featurelayerupdater.BuildConfig;
import com.sambusgeospatial.featurelayerupdater.R;
import com.sambusgeospatial.featurelayerupdater.handlers.SessionManager;
import com.sambusgeospatial.featurelayerupdater.models.SambusUser;
import com.sambusgeospatial.featurelayerupdater.onboarding.LoginActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_COUNTRY = "NAME_0";
    private static final String KEY_NAME = "NAME_1";
    private static final String KEY_CONFIRMED = "ConfCases";
    private static final String KEY_ACTIVE = "Active_Cases";
    private static final String KEY_RECOVERED = "Recovery";
    private static final String KEY_DEATHS = "Deaths";

    private Callout mCallout;
    private ServiceFeatureTable mServiceFeatureTable;
    private FeatureLayer mFeatureLayer;
    private ArcGISFeature mSelectedArcGISFeature;
    private MapView mMapView;
    private android.graphics.Point mClickPoint;

    private Snackbar mSnackbarSuccess;
    private Snackbar mSnackbarFailure;
    private String mSelectedFeatureCountry;
    private String mSelectedFeatureState;
    int mSelectedFeatureConfirmed;
    int mSelectedFeatureActive;
    int mSelectedFeatureRecovered;
    int mSelectedFeatureDeath;
    private boolean mFeatureUpdated;
    private View mCoordinatorLayout;
    private ProgressDialog mProgressDialog;
    private AppBarConfiguration mAppBarConfiguration;
    private LocationDisplay mLocationDisplay;

    private LocationDataSource.Location location;
    SessionManager sessionManager;
    SambusUser user;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sessionManager = new SessionManager(this);
        user = sessionManager.getUserDetails();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ImageView logo = headerView.findViewById(R.id.logoImage);
        TextView navFullName = headerView.findViewById(R.id.fullName);
        TextView navEmail = headerView.findViewById(R.id.email);
        final String name = user.getUserFullName() + "("+user.getUsername()+")";
        navFullName.setText(name);
        navEmail.setText(user.getUserEmail());
        // fetch the thumbnail
        byte[] imageAsBytes = Base64.decode(user.getUserThumbnail(), Base64.DEFAULT);
        logo.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // get a reference to the map view
        mMapView = findViewById(R.id.mapView);
        setupMap();
        mCoordinatorLayout = findViewById(R.id.snackbarPosition);

        // create a map with the streets basemap
        final ArcGISMap map = new ArcGISMap(Basemap.createDarkGrayCanvasVector());
        map.setInitialViewpoint(new Viewpoint(new Point(-2.460415, 13.531665, SpatialReferences.getWgs84()), 35000000));
        // set the map to be displayed in the map view
        mMapView.setMap(map);

        // get callout, set content and show
        mCallout = mMapView.getCallout();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getResources().getString(R.string.progress_title));
        mProgressDialog.setMessage(getResources().getString(R.string.progress_message));

        // create feature layer with from the service feature table
        mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.service_url));
        mFeatureLayer = new FeatureLayer(mServiceFeatureTable);

        // add the layer to the map
        map.getOperationalLayers().add(mFeatureLayer);

        // set an on touch listener to listen for click events
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                // get the point that was clicked and convert it to a point in map coordinates
                mClickPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());

                // clear any previous selection
                mFeatureLayer.clearSelection();
                mSelectedArcGISFeature = null;
                mCallout.dismiss();

                // identify the GeoElements in the given layer
                final ListenableFuture<IdentifyLayerResult> identifyFuture = mMapView
                        .identifyLayerAsync(mFeatureLayer, mClickPoint, 5, false, 1);

                // add done loading listener to fire when the selection returns
                identifyFuture.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // call get on the future to get the result
                            IdentifyLayerResult layerResult = identifyFuture.get();
                            List<GeoElement> resultGeoElements = layerResult.getElements();
                            if (!resultGeoElements.isEmpty()) {
                                if (resultGeoElements.get(0) instanceof ArcGISFeature) {
                                    mSelectedArcGISFeature = (ArcGISFeature) resultGeoElements.get(0);
                                    // highlight the selected feature
                                    mFeatureLayer.selectFeature(mSelectedArcGISFeature);
                                    // show callout with the value for the attribute KEY_STATUS of the selected feature
                                    mSelectedFeatureCountry = (String) mSelectedArcGISFeature.getAttributes().get(KEY_COUNTRY);
                                    mSelectedFeatureState = (String) mSelectedArcGISFeature.getAttributes().get(KEY_NAME);
                                    mSelectedFeatureConfirmed = (int) mSelectedArcGISFeature.getAttributes().get(KEY_CONFIRMED);
                                    mSelectedFeatureActive = (int) mSelectedArcGISFeature.getAttributes().get(KEY_ACTIVE);
                                    mSelectedFeatureRecovered = (int) mSelectedArcGISFeature.getAttributes().get(KEY_RECOVERED);
                                    mSelectedFeatureDeath = (int) mSelectedArcGISFeature.getAttributes().get(KEY_DEATHS);
                                    showCallout(mSelectedFeatureCountry, mSelectedFeatureState, mSelectedFeatureConfirmed, mSelectedFeatureActive, mSelectedFeatureRecovered, mSelectedFeatureDeath);
                                }
                            } else {
                                // none of the features on the map were selected
                                mCallout.dismiss();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Select feature failed: " + e.getMessage());
                        }
                    }
                });
                return super.onSingleTapConfirmed(e);
            }
        });

        mSnackbarSuccess = Snackbar
                .make(mCoordinatorLayout, "Feature successfully updated", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String snackBarText = updateAttributes(mSelectedFeatureConfirmed, mSelectedFeatureActive, mSelectedFeatureRecovered, mSelectedFeatureDeath) ?
                                "Feature is restored!" :
                                "Feature restore failed!";
                        Snackbar snackbar1 = Snackbar.make(mCoordinatorLayout, snackBarText, Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });
        mSnackbarFailure = Snackbar.make(mCoordinatorLayout, "Feature update failed", Snackbar.LENGTH_LONG);


    }

    private void setupMap() {
        if (mMapView != null) {
            //add arcgis runtime license key
            ArcGISRuntimeEnvironment.setLicense(BuildConfig.ARCGIS_LICENSE_KEY);

            Basemap.Type basemapType = Basemap.Type.TOPOGRAPHIC;
            double latitude = 9.4;
            double longitude = 7.29;
            int levelOfDetail = 30;
            ArcGISMap map = new ArcGISMap(basemapType, latitude, longitude, 2);
            mMapView.setMap(map);
        }
    }
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            // display progress dialog while updating attribute callout
            mProgressDialog.show();
            updateAttributes(
                    data.getIntExtra(KEY_CONFIRMED, mSelectedFeatureConfirmed),
                    data.getIntExtra(KEY_ACTIVE, mSelectedFeatureActive),
                    data.getIntExtra(KEY_RECOVERED, mSelectedFeatureRecovered),
                    data.getIntExtra(KEY_DEATHS, mSelectedFeatureDeath)
            );
        }
    }

    /**
     * Applies changes to the feature, Service Feature Table, and server.
     */
    private boolean updateAttributes(final int confirmed, final int active, final int recovered, final int deaths) {

        // load the selected feature
        mSelectedArcGISFeature.loadAsync();

        // update the selected feature
        mSelectedArcGISFeature.addDoneLoadingListener(new Runnable() {
            @Override public void run() {
                if (mSelectedArcGISFeature.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                    Log.e(TAG, "Error while loading feature");
                }

                // update the Attributes map with the new selected value for KEY_STATUS
                mSelectedArcGISFeature.getAttributes().put(KEY_CONFIRMED, confirmed);
                mSelectedArcGISFeature.getAttributes().put(KEY_ACTIVE, active);
                mSelectedArcGISFeature.getAttributes().put(KEY_RECOVERED, recovered);
                mSelectedArcGISFeature.getAttributes().put(KEY_DEATHS, deaths);

                try {
                    // update feature in the feature table
                    ListenableFuture<Void> mapViewResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature);

                    mapViewResult.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            // apply change to the server
                            final ListenableFuture<List<FeatureEditResult>> serverResult = mServiceFeatureTable.applyEditsAsync();

                            serverResult.addDoneListener(new Runnable() {
                                @Override
                                public void run() {
                                    try {

                                        // check if server result successful
                                        List<FeatureEditResult> edits = serverResult.get();
                                        if (!edits.isEmpty()) {
                                            if (!edits.get(0).hasCompletedWithErrors()) {
                                                Log.e(TAG, "Feature successfully updated");
                                                mSnackbarSuccess.show();
                                                mFeatureUpdated = true;
                                            }
                                        } else {
                                            Log.e(TAG, serverResult.toString());
                                            Log.e(TAG, "The attribute type was not changed");
                                            mSnackbarFailure.show();
                                            mFeatureUpdated = false;
                                        }
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                            // display the callout with the updated value
                                            showCallout(
                                                    (String) mSelectedArcGISFeature.getAttributes().get(KEY_COUNTRY),
                                                    (String) mSelectedArcGISFeature.getAttributes().get(KEY_NAME),
                                                    (int) mSelectedArcGISFeature.getAttributes().get(KEY_CONFIRMED),
                                                    (int) mSelectedArcGISFeature.getAttributes().get(KEY_ACTIVE),
                                                    (int) mSelectedArcGISFeature.getAttributes().get(KEY_RECOVERED),
                                                    (int) mSelectedArcGISFeature.getAttributes().get(KEY_DEATHS)
                                            );
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "applying changes to the server failed: " + e.getMessage());
                                    }
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "updating feature in the feature table failed: " + e.getMessage());
                }
            }
        });
        return mFeatureUpdated;
    }
    private void showCallout(String country, String state, int confirmed, int active, int recovered, int deaths) {
        RelativeLayout calloutLayout = new RelativeLayout(getApplicationContext());

        TextView calloutContent = new TextView(getApplicationContext());
        calloutContent.setId(R.id.textview);
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setTextSize(18);
        calloutContent.setPadding(0, 10, 10, 0);

        final String callOutText = state + ", " + country + "\n\n" +
                "Total cases: " + confirmed + "\n" +
                "Active cases: " + active + "\n" +
                "Recovered: " + recovered + "\n" +
                "Deaths: " + deaths;
        calloutContent.setText(callOutText);

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.RIGHT_OF, calloutContent.getId());

        // create image view for the callout
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_edit_location_black_24dp));
        imageView.setLayoutParams(relativeParams);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, UpdateActivity.class)
                        .putExtra(KEY_COUNTRY, country)
                        .putExtra(KEY_NAME, state)
                        .putExtra(KEY_CONFIRMED, confirmed)
                        .putExtra(KEY_ACTIVE, active)
                        .putExtra(KEY_RECOVERED, recovered)
                        .putExtra(KEY_DEATHS, deaths);
                startActivityForResult(myIntent, 100);
            }
        });

        calloutLayout.addView(calloutContent);
        calloutLayout.addView(imageView);

        mCallout.setGeoElement(mSelectedArcGISFeature, null);
        mCallout.setContent(calloutLayout);
        mCallout.show();
    }

    @Override
    protected void onPause() {
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        mMapView.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            sessionManager.logoutUser();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_exit){
            finishAndRemoveTask();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
