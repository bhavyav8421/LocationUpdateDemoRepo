package demo.location.bhavya.in.locationupdatedemoapp;

import android.text.TextUtils;

/**
 * Created by bhavyav on 16/01/17.
 */

public class Log {
    private static String TAG = "UPDATE LOCATION";
    public static void d(String s) {
        android.util.Log.d(TAG , !TextUtils.isEmpty(s) ? s : "Error");
    }

    public static void i(String s) {
        android.util.Log.i(TAG , !TextUtils.isEmpty(s) ? s : "Error");
    }
}
