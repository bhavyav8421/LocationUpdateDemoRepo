package demo.location.bhavya.in.locationupdatedemoapp;

import java.text.SimpleDateFormat;

/**
 * Created by bhavyav on 16/01/17.
 */

public class DateHelper {

    public static String retrieveTransmissionDate(long time) {
        if(time > 0) {
            SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss a");
            return df.format(time);
        }else{
            return "not available";
        }
    }
}
