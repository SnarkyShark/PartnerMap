package edu.temple.partnermap;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


// YOU NEED TO REMEMBER TO TELL IT TO IMPLEMENT THE INTERFACE
public class MapFragment extends Fragment implements OnMapReadyCallback {

    LocationManager lm;
    LocationListener ll;    // for continuous updates

    MapView mapView;
    GoogleMap map;
    Marker lastMarker;
    MapInterface parent;
    Partner[] partners;
    Marker[] partnerMarkers;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parent = (MapInterface) context; // to send info to parent activity
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        lm = getActivity().getSystemService(LocationManager.class);

        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (lastMarker != null) {
                    lastMarker.setPosition(latLng);

                } else {
                    MarkerOptions markerOptions = (new MarkerOptions())
                            .position(latLng)
                            .title("Me")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                    lastMarker = map.addMarker(markerOptions);
                }

                int zoom = 15;

                if (partners != null) {
                    displayPartnerPins();
                    Log.e(" result", "partners isn't null");

                    zoom = 1300 / (int) partners[0].distFromMe;
                    Log.e(" maptrack", "zoom: " + zoom);

                    //zoom = 10;

                    // 127 away - can't see with 15
                    // 10 shows ~100 away
                    // 5 shows north america
                    // bigger shows less
                }
                else {
                    Log.e(" result", "partners is null");
                }

                CameraUpdate cameraUpdate = CameraUpdateFactory
                        .newLatLngZoom(lastMarker.getPosition(), zoom);

                map.moveCamera(cameraUpdate);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        return v;
    }

    public void getPartnerPins(List<Partner> thePartners) {
        Log.e(" maptrack", "the map got markers");

        partners = new Partner[thePartners.toArray().length];

        thePartners.toArray(partners);

        //Log.e(" maptrack", "second partner: " + partners[1].toString());
        // so partners is converted successfully, but no pins are showing

        partnerMarkers = new Marker[partners.length];
    }

    public void displayPartnerPins() {
        int length = partners.length;
        LatLng latLng;

        for(int i=0; i<length; i++) {

            latLng = new LatLng(partners[i].latitude, partners[i].longitude);

            if (partnerMarkers[i] != null) {
                partnerMarkers[i].setPosition(latLng);

            } else {
                partnerMarkers[i] = map.addMarker(
                        new MarkerOptions()
                        .position(latLng)
                        .title(partners[i].toString()));
                Log.e(" maptrack", partners[i].toString() + ": " + partners[i].distFromMe);
                // it says that it adds all relevant markers, but i don't see any
            }
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            registerForLocationUpdates();
        }
        else
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 111); // make a constant reference

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        lm.removeUpdates(ll);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            registerForLocationUpdates();
        }
        else
            Toast.makeText(getActivity(), "Can't do that", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("MissingPermission")
    private void registerForLocationUpdates() {
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
        lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, ll);
    }

    interface MapInterface {
        public void setMyLocation(double lat, double lng);
    }

}
