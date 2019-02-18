package edu.temple.partnermap;

import android.provider.Telephony;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements MapFragment.MapInterface {

    FragmentManager fm;
    Button swapFragmentButton;

    boolean singlePane;
    boolean mapDisplayed, listDisplayed;
    MapFragment mapFragment;
    ListFragment listFragment;
    Partner[] partners;
    double myLat;
    double myLng;

    // TODO: Send my location to API
    // TODO: Allow user to input username

    /**
     * possible concerns:
     * I stopped making new instances every time i display fragments-->not sure if ok
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        singlePane = findViewById(R.id.container_2) != null;
        mapDisplayed = true;
        listDisplayed = !singlePane;
        mapFragment = new MapFragment();
        listFragment = new ListFragment();

        fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.container_1, mapFragment)
                .commit();

        // give user ability to swap fragments if there is only space for one
        if(findViewById(R.id.container_button) != null) {
            swapFragmentButton = findViewById(R.id.container_button);

            swapFragmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mapDisplayed) {
                        fm.beginTransaction()
                                .replace(R.id.container_1, listFragment)
                                .commit();
                        swapFragmentButton.setText(R.string.show_map_button);
                        mapDisplayed = false;
                        listDisplayed = true;
                    }
                    else {
                        fm.beginTransaction()
                                .replace(R.id.container_1, mapFragment)
                                .commit();
                        swapFragmentButton.setText(R.string.show_list_button);
                        mapDisplayed = true;
                        listDisplayed = false;
                    }
                }
            });
        }
        else {  // if there is no button to swap, then both panes should display
            fm.beginTransaction()
                    .replace(R.id.container_2, listFragment)
                    .commit();
        }

        // TODO: update this every 30 seconds
        getPartners();
    }

    public void getPartners() {

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = "https://kamorris.com/lab/get_locations.php";

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        int len = response.length();

                        Log.e(" result",(String.valueOf(response)));
                        Log.e(" result","response length: " + len);

                        partners = new Partner[len];
                        for(int i=0; i<len; i++) {
                            try {
                                JSONObject jresponse = response.getJSONObject(i);
                                String user = jresponse.getString("username");
                                double lat = jresponse.getDouble("latitude");
                                double lng = jresponse.getDouble("longitude");
                                double dist = distance(lat, lng);

                                partners[i] = new Partner(user, lat, lng, dist);
                                Log.e(" result","partner dist: " + partners[i].distFromMe);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(" result","lol nope");
                            }
                        }
                        if(listDisplayed)
                            listFragment.makeAndAssignPartnerData(partners);
                        else if(mapDisplayed)
                            Log.i(" result", "map stuff");
                        else
                            Log.e(" result", "nothing's displaying?");
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        requestQueue.add(getRequest);

        // sample data
        /*partners = new Partner[5];
        partners[0] = new Partner("steve top", 123, 123, 5);
        partners[1] = new Partner("steve bot", 123, 123, 9);
        partners[2] = new Partner("steve mid", 123, 123, 7);
        partners[3] = new Partner("bob high", 123, 123, 6);
        partners[4] = new Partner("bob low", 123, 123, 8); */
    }

    public double distance(double friendLat, double friendLng) {
        double lats = myLat - friendLat;
        double lngs = myLng - friendLng;
        return Math.sqrt(lats*lats + lngs*lngs);
    }

    @Override
    public void setMyLocation(double lat, double lng) {
        myLat = lat;
        myLng = lng;
    }
}
