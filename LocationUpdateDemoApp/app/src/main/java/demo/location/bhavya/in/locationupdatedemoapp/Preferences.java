package demo.location.bhavya.in.locationupdatedemoapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bhavyav on 15/01/17.
 */

public class Preferences {

    private static String NAME_KEY = "name";
    private static String PREFS_NAME = "LOCATION_UPDATE";
    private static String PREFS_LAST_UPDATE_TIME = "last_updated_time";

    private static SharedPreferences preferences;

    private static SharedPreferences getSharedPref(Context ctx) {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(PREFS_NAME , Context.MODE_PRIVATE);
        }
        return preferences;
    }


    public static void setNameInPrefs(Context context, String value) {
        SharedPreferences.Editor edit = getSharedPref(context).edit();
        edit.putString(NAME_KEY, value);
        edit.commit();
    }

    public static String getNameFromPrefs(Context applicationContext){
        SharedPreferences sharedPref = getSharedPref(applicationContext);
        return sharedPref.getString(NAME_KEY , "John Doe");
    }

    public static void setLastUpdateTimeInPrefs(Context context, long value) {
        SharedPreferences.Editor edit = getSharedPref(context).edit();
        edit.putLong(PREFS_LAST_UPDATE_TIME, value);
        edit.commit();
    }

    public static long getLastUpdateTimeFromPrefs(Context applicationContext){
        SharedPreferences sharedPref = getSharedPref(applicationContext);
        return sharedPref.getLong(PREFS_LAST_UPDATE_TIME , 0);
    }
}
