package edu.temple.partnermap;

import android.support.annotation.NonNull;

public class Partner {

    public String username;
    public double latitude, longitude, distFromMe;

    public Partner(String user, double lat, double lng, double dist){
        username = user;
        latitude = lat;
        longitude = lng;
        distFromMe = dist;
    }

    public int compareTo(Partner p) {
        int retval = 0;
        double signNum = this.distFromMe - p.distFromMe;

        if(signNum < 0)
            retval = -1;
        else if (signNum > 0)
            retval = 1;
        //else, retval = 0
        return retval;
    }

    @Override
    @NonNull
    public String toString() {
        return this.username;
    }
}
