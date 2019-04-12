package edu.temple.mapchat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // username
    String username;
    EditText usernameEditText;
    Button usernameButton;
    private SharedPreferences sharedPref;
    private static final String USER_PREF_KEY = "USERNAME_PREF";

    // map
    private LocationManager lm;
    private LocationListener ll;
    private GoogleMap mMap;
    private Marker lastMarker;
    private Location mLocation;
    private HashMap<String,Marker> mMarkers = new HashMap<>();
    private static final String LOC_LAT = "LAST_LAT";
    private static final String LOC_LNG = "LAST_LNG";
    private static final String LOC_PRO = "LAST_PRO";

    // list of friends
    ListView listView;
    ArrayAdapter<String> friendNamesAdapter;
    ArrayList<String> friendNames;
    public static final String EXTRA_FRIEND = "";

    // api
    private RequestQueue queue;
    ArrayList<Friend> friends;
    private String getFriendsUrl = "https://kamorris.com/lab/get_locations.php";
    private String postPosUrl = "https://kamorris.com/lab/register_location.php";

    // this is a test change to try to get github & logcat working again
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // hooking up screen elements
        usernameEditText = findViewById(R.id.usernameEditText);
        usernameButton = findViewById(R.id.usernameButton);
        listView = findViewById(R.id.listView);

        // initialize values
        friends = new ArrayList<>();
        friendNames = new ArrayList<>();
        sharedPref = getSharedPreferences("myMapChatApp", Context.MODE_PRIVATE);

        // check/set username
        checkUsername();
        usernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUsername();
            }
        });

        // map
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SupportMapFragment mapFragment = new SupportMapFragment();
        transaction.replace(R.id.mapView, mapFragment).commit();
        mapFragment.getMapAsync(MainActivity.this);

        lm = getSystemService(LocationManager.class);
        ll = makeLocationListener();

        String lat = sharedPref.getString(LOC_LAT, null);
        String lon = sharedPref.getString(LOC_LNG, null);
        mLocation = null;
        if (lat != null && lon != null) {
            String provider = sharedPref.getString(LOC_PRO, null);
            mLocation = new Location(provider);
            mLocation.setLatitude(Double.parseDouble(lat));
            mLocation.setLongitude(Double.parseDouble(lon));
        }

        // list of friends
        friendNamesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, friendNames);
        listView.setAdapter(friendNamesAdapter);

        // api
        queue = Volley.newRequestQueue(this);

        // update partnermap & listview every 30 seconds
        final Handler handler = new Handler();
        final int delay = 30000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                get();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    /**
     * Check username
     * Set username
     * Generate public & private keys
     */
    private void checkUsername() {
        String storedUsername = sharedPref.getString(USER_PREF_KEY, null);
        if(storedUsername != null) {
            username = storedUsername;
            post();
            Log.e(" posttest", "tried to call method");
            setTitle("username: " + username);
        }
        else
            Toast.makeText(this, "please enter a username", Toast.LENGTH_SHORT).show();
    }

    private void setUsername() {
        boolean usernameReady;

        // generate new username
        username = usernameEditText.getText().toString();
        usernameReady = username.compareTo("") != 0;
        if (usernameReady) {
            usernameReady = sharedPref.edit().putString(USER_PREF_KEY, username).commit();
        }
        if (usernameReady) {
            setTitle("username: " + username);
        }
        else {
            Toast.makeText(this, "please enter a valid username", Toast.LENGTH_SHORT).show();
            Log.e( "usertrack", "username wasn't saved to shared preferences");
        }
        post();
        Log.e(" posttest", "tried to call method");

    }

    /**
     * Get and Post Partner Data
     */
    private void get() {
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, getFriendsUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int len = response.length();
                        friends = new ArrayList<>();
                        for(int i=0; i<len; i++) {
                            String user = "";
                            double lat = 0, lng = 0;
                            try {
                                JSONObject jresponse = response.getJSONObject(i);
                                user = jresponse.getString("username");
                                lat = jresponse.getDouble("latitude");
                                lng = jresponse.getDouble("longitude");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(" friendtest", "json issue");
                            }

                            Friend newPal = new Friend(user, lat, lng);
                            if (mLocation != null)
                                newPal.calculateDist(mLocation.getLatitude(), mLocation.getLongitude());
                            else
                                newPal.calculateDist(lat, lng);

                            friends.add(newPal);
                            Log.e(" friendtest", "added friend: " + newPal + ": " + newPal.getDistance() + " away");

                        }
                        Log.e(" friendtest", "successful volley request");

                        updateFriendList(len);
                        updateMarkerList(len);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(" friendtest", "volley request failed");
                CameraUpdate cameraUpdate = CameraUpdateFactory
                        .newLatLngZoom(lastMarker.getPosition(), 14);

                mMap.moveCamera(cameraUpdate);
            }
        });
        queue.add(getRequest);
    }

    public void updateMarkerList(int len) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i=0; i<len; i++) {
            String name = friends.get(i).toString();
            Marker marker = mMarkers.get(name);
            if(marker == null) {
                marker = mMap.addMarker(new MarkerOptions().title(name)
                        .position(friends.get(i).getPosition()));
                mMarkers.put(name, marker);
            }
            else{
                marker.remove();
                marker = mMap.addMarker(new MarkerOptions().title(name)
                        .position(friends.get(i).getPosition()));
                mMarkers.put(name, marker);
            }
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 40;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

        public void updateFriendList(int len) {
        try {   // convert Friend arraylist to String arraylist
            friendNames.clear();
            Collections.sort(friends);
            for(int i=0; i<len; i++) {
                Log.e(" friendsort", "map: " + friends.get(i).toString());
                friendNames.add(friends.get(i).toString());
            }
        } catch (Exception e) {
            Log.e(" friendsort", "the toggle switch isn't toggling");
        }
        friendNamesAdapter.notifyDataSetChanged();
    }

    private void post() {
        Log.d("Post stuff", username + ", " + mLocation);
        if(username == null || username.equals("") || mLocation == null){
            Log.e(" posttest", "values were null");
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, postPosUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Volley Result", ""+ response);
                Log.e(" posttest", "got a response");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(" posttest", "got a error response");
            }
        }){

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                Log.e(" posttest", "tried to post");
                postMap.put("user", username);
                postMap.put("latitude", "" + mLocation.getLatitude());
                postMap.put("longitude", "" + mLocation.getLongitude());
                return postMap;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
        get();
    }

    /**
     * Google Map View
     */
    // track your current location
    private LocationListener makeLocationListener(){
        Log.e( "marktrack", "kicked off makeLocationlistener");
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e( "marktrack", "location changed");
                float distance = mLocation.distanceTo(location);
                if(distance > 10)   // reported when user moves 10 meters
                {
                    mLocation = location;
                    post();
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) { }
            @Override
            public void onProviderDisabled(String provider) { }
        };
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(" map", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(" map", "Can't find style. Error: ", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            registerForLocationUpdates();
        else
            Toast.makeText(this, "No map permission", Toast.LENGTH_LONG).show();
    }
    @SuppressLint("MissingPermission")
    private void registerForLocationUpdates() {
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
        lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, ll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(" posttest", "we resumed");

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            registerForLocationUpdates();
        else
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 111); // make a constant reference

        get();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(ll);
        if (mLocation != null) {
            sharedPref.edit().putString(LOC_LAT, String.valueOf(mLocation.getLatitude())).apply();
            sharedPref.edit().putString(LOC_LNG, String.valueOf(mLocation.getLongitude())).apply();
            sharedPref.edit().putString(LOC_PRO, mLocation.getProvider()).apply();

        }
    }

}