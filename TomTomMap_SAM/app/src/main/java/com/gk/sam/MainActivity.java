package com.gk.sam;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import android.Manifest;
import android.content.pm.PackageManager;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gk.sam.adapter.PlaceAutoCompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.location.LatLngAcc;
import com.tomtom.online.sdk.common.rx.RxContext;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapConstants;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerAnchor;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.map.model.MapLayersType;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.InstructionsType;
import com.tomtom.online.sdk.routing.data.Report;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.routing.data.TravelMode;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.api.SearchError;
import com.tomtom.online.sdk.search.api.fuzzy.FuzzySearchResultListener;
import com.tomtom.online.sdk.search.api.revgeo.RevGeoSearchResultListener;
import com.tomtom.online.sdk.search.data.common.Address;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQuery;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQuery;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQueryBuilder;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchResponse;
import com.tomtom.online.sdk.search.extensions.SearchService;
import com.tomtom.online.sdk.search.extensions.SearchServiceConnectionCallback;
import com.tomtom.online.sdk.search.extensions.SearchServiceManager;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends FragmentActivity implements SearchServiceConnectionCallback, RxContext, PlaceAutoCompleteAdapter.ClickPlaceInterface {

    private static final String TAG = MainActivity.class.getName();
    private static final int NETWORK_THREADS_NUMBER = 4;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    // permissions request code
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private Boolean mLocationPermissionGranted = false;
    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    private TomtomMap mTomtomMap = null;
    private MapFragment mapFragment = null;
    private LinearLayout llTravelMode;
    private Location mLastKnownLocation;
    private Marker mCurrLocationMarker, mDestLocationMarker;
    private PlaceAutoCompleteAdapter placeAutoCompleteAdapter;
    private RecyclerView lvPlaces;
    private EditText edtSearch;
    private Button btnDirection, btnCar, btnTruck, btnWalk;
    private boolean isTrafficOn = false;
    private boolean isHybridOn = false;
    private SettingsClient mSettingsClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private SearchService searchService;
    private ImmutableList<FuzzySearchResult> lastSearchResult;
    public static final int STANDARD_RADIUS = 30 * 1000; //30 km
    private LatLng currentPosition, destinationPosition;
    private ServiceConnection searchServiceConnection;
    private Animation animIn;
    private TravelMode travelMode = TravelMode.CAR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("last_known_location")) {
                mLastKnownLocation = savedInstanceState.getParcelable("last_known_location");
            }
        }
        // Checks the dynamically controlled permissions and requests missing permissions from end user.
        if (PermissionManager.hasLocationPermissions(this)) {
            mLocationPermissionGranted = true;
            initialize();
        } else {
            PermissionManager.requestLocationPermissions(MainActivity.this);
        }
    }

    private void initialize() {
        setContentView(R.layout.activity_main);
        if (isGooglePlayServicesAvailable()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mSettingsClient = LocationServices.getSettingsClient(this);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    // location is received
                    mLastKnownLocation = locationResult.getLastLocation();
                    if (mLastKnownLocation != null) {
                        currentPosition = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                    }
                }
            };
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(60000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            mLocationSettingsRequest = builder.build();

        }
        ImageButton btnCurrentLocation = (ImageButton) findViewById(R.id.btnCurrentLocation);
        llTravelMode = (LinearLayout) findViewById(R.id.llTravelMode);

        btnDirection = (Button) findViewById(R.id.btnDirection);
        btnCar = (Button) findViewById(R.id.btnCar);
        btnTruck = (Button) findViewById(R.id.btnTruck);
        btnWalk = (Button) findViewById(R.id.btnWalk);
        edtSearch = (EditText) findViewById(R.id.edtSearch);
        ImageButton btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchDialog();
            }
        });
        lvPlaces = (RecyclerView) findViewById(R.id.lvPlaces);
        lvPlaces.setHasFixedSize(true);
        lvPlaces.setLayoutManager(new LinearLayoutManager(this));

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        });
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().equals("")) {
                    if (mLastKnownLocation != null) {
                        SearchApi searchAPI = OnlineSearchApi.create(MainActivity.this);
                        FuzzySearchQuery fuzzySearchQuery;
                        if (currentPosition != null) {
                            fuzzySearchQuery = FuzzySearchQueryBuilder.create(s.toString().trim()).withPreciseness(new LatLngAcc(currentPosition, STANDARD_RADIUS)).build();
                        } else {
                            fuzzySearchQuery = FuzzySearchQueryBuilder.create(s.toString().trim()).build();
                        }
                        searchAPI.search(fuzzySearchQuery, new FuzzySearchResultListener() {
                            @Override
                            public void onSearchResult(FuzzySearchResponse fuzzySearchResponse) {
                                final ImmutableList<FuzzySearchResult> fuzzySearchResults = fuzzySearchResponse.getResults();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        placeAutoCompleteAdapter = new PlaceAutoCompleteAdapter(MainActivity.this, fuzzySearchResults);
                                        lvPlaces.setAdapter(placeAutoCompleteAdapter);

                                    }
                                });
                            }

                            @Override
                            public void onSearchError(SearchError searchError) {
                                Toast.makeText(MainActivity.this,
                                        "Failed to search.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        final Button btnSatellite = (Button) findViewById(R.id.btnSatellite);
        ImageButton btnShare = (ImageButton) findViewById(R.id.btnShare);
        final ImageButton btnTransit = (ImageButton) findViewById(R.id.btnTransit);
        btnTransit.setAlpha((float) 0.6);
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment.getAsyncMap(onMapReadyCallback);

        btnDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDirection.setBackgroundResource(R.drawable.ic_directions_stop);
            }
        });


        btnCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCar.setAlpha((float) 1);
                btnTruck.setAlpha((float) 0.6);
                btnWalk.setAlpha((float) 0.6);
                travelMode = TravelMode.CAR;
                createRoute();
            }
        });

        btnTruck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCar.setAlpha((float) 0.6);
                btnTruck.setAlpha((float) 1);
                btnWalk.setAlpha((float) 0.6);
                travelMode = TravelMode.TRUCK;
                createRoute();
            }
        });

        btnWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCar.setAlpha((float) 0.6);
                btnTruck.setAlpha((float) 0.6);
                btnWalk.setAlpha((float) 1);
                travelMode = TravelMode.PEDESTRIAN;
                createRoute();
            }
        });

        btnCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utility.checkGPS(MainActivity.this)) {
                    alertNoGPS();
                } else {
                    getDeviceLocation();
                }
            }
        });
        btnTransit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTrafficOn) {
                    mTomtomMap.getUiSettings().turnOnVectorTrafficFlowTiles();
                    btnTransit.setAlpha((float) 1);
                    isTrafficOn = true;
                } else {
                    btnTransit.setAlpha((float) 0.6);
                    mTomtomMap.getUiSettings().turnOffTraffic();
                    mTomtomMap.getUiSettings().turnOffTrafficFlowTiles();
                    isTrafficOn = false;
                }
            }
        });
        btnSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isHybridOn) {
                    mTomtomMap.getUiSettings().setMapLayersType(MapLayersType.HYBRID);
                    btnSatellite.setBackgroundResource(R.drawable.ic_map_view);
                    isHybridOn = true;
                } else {
                    mTomtomMap.getUiSettings().setMapLayersType(MapLayersType.NONE);
                    btnSatellite.setBackgroundResource(R.drawable.ic_satellite);
                    isHybridOn = false;
                }
            }
        });
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mLastKnownLocation != null) {
                    String location = "I'm currently at location  http://maps.google.com/?q=" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, location);
                    sendIntent.setType("text/plain");
                    Intent createChooser = Intent.createChooser(sendIntent, "");
                    createChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(createChooser);
                }
            }
        });
        hideSoftwareKeyboard(edtSearch);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    private OnMapReadyCallback onMapReadyCallback =
            new OnMapReadyCallback() {
                @Override
                public void onMapReady(TomtomMap map) {
                    //Map is ready here
                    mTomtomMap = map;
                    mTomtomMap.setMyLocationEnabled(true);
                    mTomtomMap.getUiSettings().setMapLayersType(MapLayersType.NONE);
                    mTomtomMap.setLanguage(Locale.getDefault().getLanguage());
                    mTomtomMap.getMarkerSettings().addOnMarkerDragListener(onMarkerDragListener);
                    mTomtomMap.addOnMapClickListener(onMapClickListener);
                }
            };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionManager.RESULT_PERMISSION_LOCATION:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                mLocationPermissionGranted = true;
                initialize();
                break;
        }
    }


    private void getDeviceLocation() {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the mTomtomMap's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                SearchApi searchApi = createSearchAPI();
                                ReverseGeocoderSearchQuery reverseGeocoderQuery =
                                        createReverseGeocoderQuery(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                searchApi.reverseGeocoding(reverseGeocoderQuery, new RevGeoSearchResultListener() {
                                    @Override
                                    public void onSearchResult(ReverseGeocoderSearchResponse reverseGeocoderSearchResponse) {
                                        if (reverseGeocoderSearchResponse.hasResults()) {
                                            Address address = reverseGeocoderSearchResponse.getAddresses().get(0).getAddress();
                                            String freeformAddress = address.getFreeformAddress();
                                            if (!Strings.isNullOrEmpty(freeformAddress)) {
                                                if (mTomtomMap != null && mCurrLocationMarker != null) {
                                                    mTomtomMap.removeMarker(mCurrLocationMarker);
                                                    mCurrLocationMarker = null;
                                                }
                                                currentPosition = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                                AddMarkerCurrentLocation(freeformAddress);
                                            }

                                        }

                                    }

                                    @Override
                                    public void onSearchError(SearchError searchError) {
                                        Utility.myWarningAlert(MainActivity.this, searchError.getMessage());
                                    }
                                });
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, status, 0).show();
            return false;
        }
    }

    private void AddMarkerCurrentLocation(String _address) {

        MarkerBuilder markerBuilder = new MarkerBuilder(currentPosition);
        markerBuilder.markerBalloon(new SimpleMarkerBalloon(_address));
        markerBuilder.icon(Icon.Factory.fromResources(this, R.drawable.ic_current_location_marker));
        markerBuilder.tag("1").iconAnchor(MarkerAnchor.Bottom);
        if (mTomtomMap != null) {
            mCurrLocationMarker = mTomtomMap.addMarker(markerBuilder);
            mTomtomMap.centerOn(
                    currentPosition.getLatitude(),
                    currentPosition.getLongitude(),
                    10,
                    MapConstants.ORIENTATION_NORTH
            );
        }
    }

    private void addMarkerAtPlace(String _address, LatLng position) {

        MarkerBuilder markerBuilder = new MarkerBuilder(position);
        markerBuilder.markerBalloon(new SimpleMarkerBalloon(_address));
        markerBuilder.icon(Icon.Factory.fromResources(this, R.drawable.ic_places));
        if (mTomtomMap != null) {
            mTomtomMap.addMarker(markerBuilder);
        }

    }


    private void AddMarkerDestinationLocation(String _address, LatLng position) {

        if (mTomtomMap != null && mDestLocationMarker != null) {
            mTomtomMap.removeMarker(mDestLocationMarker);
            mDestLocationMarker = null;
        }

        MarkerBuilder markerBuilder = new MarkerBuilder(position);
        markerBuilder.markerBalloon(new SimpleMarkerBalloon(_address));
        markerBuilder.icon(Icon.Factory.fromResources(this, R.drawable.ic_marker));
        markerBuilder.tag("1");
        markerBuilder.draggable(true);
        if (mTomtomMap != null) {
            mDestLocationMarker = mTomtomMap.addMarker(markerBuilder);
            mTomtomMap.centerOn(
                    position.getLatitude(),
                    position.getLongitude(),
                    10,
                    MapConstants.ORIENTATION_NORTH
            );
        }

        createRoute();


    }

    @Override
    public void onPlaceClick(ImmutableList<FuzzySearchResult> mResultList, int position, String address) {
        if (mResultList != null) {
            //btnDirection.setVisibility(View.VISIBLE);
            llTravelMode.setVisibility(View.VISIBLE);
            destinationPosition = mResultList.get(position).getPosition();
            AddMarkerDestinationLocation(address, mResultList.get(position).getPosition());
            if (placeAutoCompleteAdapter != null) {
                placeAutoCompleteAdapter.clearList();
                lvPlaces.setAdapter(null);
            }
            hideSoftwareKeyboard(edtSearch);
        }

    }

    /**
     * Create Route
     */
    private void createRoute() {
        if (mDestLocationMarker != null && mCurrLocationMarker != null) {
            if (mTomtomMap != null) {
                mTomtomMap.clearRoute();
            }
            showRoute(getRouteQuery(travelMode));
        }
    }

    /**
     * Show Search Type Dialog
     */
    private void showSearchDialog() {

        try {
            final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.serach_list_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            animIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
            WindowManager.LayoutParams wlp = window.getAttributes();
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            wlp.gravity = Gravity.TOP;
            LinearLayout llContentContainer = (LinearLayout) dialog.findViewById(R.id.llContentContainer);
            final EditText edtSearch = (EditText) dialog.findViewById(R.id.edtSearch);
            Button btnAround = (Button) dialog.findViewById(R.id.btnAround);
            Button btnExplore = (Button) dialog.findViewById(R.id.btnExplore);
            Button btnHotels = (Button) dialog.findViewById(R.id.btnHotels);
            Button btnSearch = (Button) dialog.findViewById(R.id.btnSearch);
            btnAround.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cleanMap();
                    FuzzySearchQuery query = FuzzySearchQueryBuilder.create("Restaurant")
                            .withPreciseness(new LatLngAcc(currentPosition, STANDARD_RADIUS))
                            .withTypeAhead(true)
                            .withCategory(true).build();
                    performSearch(query);
                }
            });
            btnExplore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cleanMap();
                    FuzzySearchQuery query = FuzzySearchQueryBuilder.create("SHOPPING")
                            .withPreciseness(new LatLngAcc(currentPosition, STANDARD_RADIUS))
                            .withTypeAhead(true)
                            .withCategory(true).build();

                    performSearch(query);
                }
            });
            btnHotels.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cleanMap();
                    FuzzySearchQuery query = FuzzySearchQueryBuilder.create("Hotels")
                            .withPreciseness(new LatLngAcc(currentPosition, STANDARD_RADIUS))
                            .withTypeAhead(true)
                            .withCategory(true).build();

                    performSearch(query);
                }
            });
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!edtSearch.getText().toString().trim().equals("")) {
                        cleanMap();
                        FuzzySearchQuery query = FuzzySearchQueryBuilder.create(edtSearch.getText().toString().trim()).withPreciseness(new LatLngAcc(currentPosition, STANDARD_RADIUS)).build();
                        performSearch(query);
                    }
                }
            });
            llContentContainer.startAnimation(animIn);
            dialog.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    private void cleanMap() {
        mTomtomMap.removeMarkers();
    }

    private SearchApi createSearchAPI() {
        SearchApi searchApi = OnlineSearchApi.create(this);
        return searchApi;
    }

    private ReverseGeocoderSearchQuery createReverseGeocoderQuery(double latitude, double longitude) {
        return ReverseGeocoderSearchQueryBuilder.create(latitude, longitude).build();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("last_known_location", mLastKnownLocation);
    }

    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {

                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, 100);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG, "Location updates stopped!");
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // pausing location updates
        stopLocationUpdates();
        hideSoftwareKeyboard(edtSearch);
        //tag::doc_search_service_unbinding[]
        SearchServiceManager.unbind(this, searchServiceConnection);
    }


    @Override
    protected void onResume() {
        super.onResume();
        startAndBindToSearchService();
        hideSoftwareKeyboard(edtSearch);
        if (mLocationPermissionGranted) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                getDeviceLocation();
                break;
        }
    }

    private void alertNoGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enable location based services for accurate data!")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void hideSoftwareKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onBindSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void cancelPreviousSearch() {
        getSearchProvider().cancelSearchIfRunning();
    }

    private SearchService getSearchProvider() {
        return searchService;
    }

    private void performSearch(FuzzySearchQuery query) {
        cancelPreviousSearch();
        performSearchWithoutBlockingUI(query);
    }

    private void performSearchWithoutBlockingUI(FuzzySearchQuery query) {
        CustomProgressDialog.showProgressDialog(MainActivity.this, "", false);
        lastSearchResult = null;
        getSearchProvider()
                .search(query)
                .subscribeOn(getWorkingScheduler())
                .observeOn(getResultScheduler())
                .subscribe(new DisposableSingleObserver<FuzzySearchResponse>() {
                    @Override
                    public void onSuccess(FuzzySearchResponse fuzzySearchResponse) {
                        lastSearchResult = fuzzySearchResponse.getResults();
                        CustomProgressDialog.removeDialog();
                        for (FuzzySearchResult fuzzySearchResult : lastSearchResult) {
                            Address address = fuzzySearchResult.getAddress();
                            addMarkerAtPlace(fuzzySearchResult.getPoi().getName(), fuzzySearchResult.getPosition());
                        }
                        mTomtomMap.getMarkerSettings().zoomToAllMarkers();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Utility.myWarningAlert(MainActivity.this, throwable.getMessage());
                        CustomProgressDialog.removeDialog();
                    }
                });
    }

    @NonNull
    @Override
    public Scheduler getWorkingScheduler() {
        return Schedulers.from(Executors.newFixedThreadPool(NETWORK_THREADS_NUMBER));
    }

    @NonNull
    @Override
    public Scheduler getResultScheduler() {
        return AndroidSchedulers.mainThread();
    }

    protected void startAndBindToSearchService() {
        searchServiceConnection = SearchServiceManager.createAndBind(this,
                this);
    }

    class searchAddressTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    TomtomMapCallback.OnMarkerDragListener onMarkerDragListener = new TomtomMapCallback.OnMarkerDragListener() {
        @Override
        public void onStartDragging(@NonNull Marker marker) {

        }

        @Override
        public void onStopDragging(@NonNull Marker marker) {
            CustomProgressDialog.showProgressDialog(MainActivity.this, "", false);
            destinationPosition = marker.getPosition();
            if (mTomtomMap != null && mDestLocationMarker != null) {
                mTomtomMap.removeMarker(mDestLocationMarker);
                mDestLocationMarker = null;
            }
            mDestLocationMarker = marker;
            SearchApi searchApi = createSearchAPI();
            ReverseGeocoderSearchQuery reverseGeocoderQuery =
                    createReverseGeocoderQuery(mDestLocationMarker.getPosition().getLatitude(), mDestLocationMarker.getPosition().getLongitude());
            searchApi.reverseGeocoding(reverseGeocoderQuery, new RevGeoSearchResultListener() {
                @Override
                public void onSearchResult(ReverseGeocoderSearchResponse reverseGeocoderSearchResponse) {
                    if (reverseGeocoderSearchResponse.hasResults()) {
                        Address address = reverseGeocoderSearchResponse.getAddresses().get(0).getAddress();
                        String freeformAddress = address.getFreeformAddress();
                        if (!Strings.isNullOrEmpty(freeformAddress)) {
                            MarkerBuilder markerBuilder = new MarkerBuilder(mDestLocationMarker.getPosition());
                            markerBuilder.markerBalloon(new SimpleMarkerBalloon(freeformAddress));
                            markerBuilder.icon(Icon.Factory.fromResources(MainActivity.this, R.drawable.ic_marker));
                            markerBuilder.tag("1");
                            markerBuilder.draggable(true);
                            if (mTomtomMap != null) {
                                mDestLocationMarker = mTomtomMap.addMarker(markerBuilder);
                                mTomtomMap.centerOn(
                                        destinationPosition.getLatitude(),
                                        destinationPosition.getLongitude(),
                                        10,
                                        MapConstants.ORIENTATION_NORTH
                                );
                            }
                            createRoute();
                        }
                    }
                    CustomProgressDialog.removeDialog();
                }

                @Override
                public void onSearchError(SearchError searchError) {
                    Utility.myWarningAlert(MainActivity.this, searchError.getMessage());
                    CustomProgressDialog.removeDialog();
                }
            });


        }

        @Override
        public void onDragging(@NonNull Marker marker) {

        }
    };


    private TomtomMapCallback.OnMapClickListener onMapClickListener =
            new TomtomMapCallback.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    SearchApi searchApi = createSearchAPI();
                    ReverseGeocoderSearchQuery reverseGeocoderQuery =
                            createReverseGeocoderQuery(latLng.getLatitude(), latLng.getLongitude());
                    searchApi.reverseGeocoding(reverseGeocoderQuery, new RevGeoSearchResultListener() {
                        @Override
                        public void onSearchResult(ReverseGeocoderSearchResponse reverseGeocoderSearchResponse) {
                            if (reverseGeocoderSearchResponse.hasResults()) {
                                Address address = reverseGeocoderSearchResponse.getAddresses().get(0).getAddress();
                                String freeformAddress = address.getFreeformAddress();
                                if (!Strings.isNullOrEmpty(freeformAddress)) {
                                    Utility.ShowCustomToast(MainActivity.this, freeformAddress);
                                }
                            }
                        }

                        @Override
                        public void onSearchError(SearchError searchError) {
                            Utility.myWarningAlert(MainActivity.this, searchError.getMessage());
                        }
                    });
                }
            };

    private RouteQuery getRouteQuery(TravelMode travelMode) {
        RouteQuery queryBuilder = RouteQueryBuilder.create(currentPosition, destinationPosition)
                .withMaxAlternatives(0)
                .withReport(Report.EFFECTIVE_SETTINGS)
                .withInstructionsType(InstructionsType.TEXT)
                .withTravelMode(travelMode)
                .withRouteType(RouteType.FASTEST).build();
        return queryBuilder;
    }

    protected void displayRoutes(RouteResponse routeResponse) {
        displayFullRoutes(routeResponse);
        mTomtomMap.displayRoutesOverview();
    }

    private void showRoute(RouteQuery routeQuery) {
        RoutingApi routePlannerAPI = OnlineRoutingApi.create(MainActivity.this);
        Disposable subscribe = routePlannerAPI.planRoute(routeQuery).subscribeOn(getWorkingScheduler())
                .observeOn(getResultScheduler())
                .subscribe(new Consumer<RouteResponse>() {
                    @Override
                    public void accept(RouteResponse routeResult) throws Exception {
                        displayRoutes(routeResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Utility.myWarningAlert(MainActivity.this, throwable.getMessage());
                    }
                });
        compositeDisposable.add(subscribe);
    }

    private void displayFullRoutes(RouteResponse routeResponse) {
        List<FullRoute> routes = routeResponse.getRoutes();
        Optional<FullRoute> activeRoute = getActiveRoute(routes);
        for (FullRoute route : routes) {
            boolean isActiveRoute = activeRoute.isPresent() ? activeRoute.get().equals(route) : false;
            RouteBuilder routeBuilder = new RouteBuilder(route.getCoordinates())
                    .isActive(isActiveRoute);
            mTomtomMap.addRoute(routeBuilder);
        }
    }

    private Optional<FullRoute> getActiveRoute(List<FullRoute> fullRoutes) {
        if (fullRoutes != null && !fullRoutes.isEmpty()) {
            return Optional.of(fullRoutes.get(0));
        }
        return Optional.absent();
    }

}
