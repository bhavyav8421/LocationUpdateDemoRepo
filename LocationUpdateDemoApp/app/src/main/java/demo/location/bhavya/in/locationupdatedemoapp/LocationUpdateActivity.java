package demo.location.bhavya.in.locationupdatedemoapp;

import android.*;
import android.Manifest;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class LocationUpdateActivity extends LocationActivity{

    private String name;
    // UI references.
    private AutoCompleteTextView nameEditText;
    private TextView locationUpdateTextView , lastUpdateTimeTextView;
    private boolean isSubmitted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_update);
        nameEditText = (AutoCompleteTextView) findViewById(R.id.name);
        locationUpdateTextView = (TextView) findViewById(R.id.locationUpdateTextView);
        lastUpdateTimeTextView = (TextView) findViewById(R.id.lastUpdatedTime);
        name = Preferences.getNameFromPrefs(getApplicationContext());
        nameEditText.setText(name);

        nameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    updateLocation();
                    return true;
                }
                return false;
            }
        });

        Button submitButton = (Button) findViewById(R.id.update_location_button);
        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLocation();
            }
        });

    }

    private void updateLocation() {

        // Reset errors.
        nameEditText.setError(null);
        String email = nameEditText.getText().toString();

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            nameEditText.setError(getString(R.string.error_field_required));
            nameEditText.requestFocus();
        }
    }

    public void updateLocationChanged(Location location){
        updateUI(location);
    }

    private void updateUI(Location location){
        locationUpdateTextView.setText(location.getLatitude() +", "+location.getLongitude());
        String latlng = location.getLatitude()+"/"+location.getLongitude();
        if(!TextUtils.isEmpty(nameEditText.getText())) {
            new PostClient().execute(latlng);
        }
    }


    class PostClient extends AsyncTask<String, Void, String> {

        public String doInBackground(String... latlng) {

            // Predefine variables
            String io = new String(latlng[0]);
            URL url;

            try {
                // Stuff variables
                url = new URL("http://gentle-tor-1851.herokuapp.com/events");
                String param = "data=NAME is now at "+io;

                // Open a connection using HttpURLConnection
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setReadTimeout(7000);
                con.setConnectTimeout(7000);
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setInstanceFollowRedirects(false);
                con.setRequestMethod("POST");
                con.setFixedLengthStreamingMode(param.getBytes().length);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                byte[] userNamePswd = ("parzival:wade3").getBytes();
                String encoded = Base64.encodeToString(userNamePswd , Base64.DEFAULT);
                con.setRequestProperty("Authorization", "Basic "+encoded);

                // Send
                PrintWriter out = new PrintWriter(con.getOutputStream());
                out.print(param);
                out.close();

                con.connect();

                BufferedReader in = null;
                if (con.getResponseCode() != 201) {
                    in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    Preferences.setLastUpdateTimeInPrefs(getApplicationContext() , System.currentTimeMillis());
                };

            } catch (Exception e) {
                Log.d(e.getMessage());
                e.printStackTrace();
                return null;
            }
            // Set null and we´e good to go
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            lastUpdateTimeTextView.setText("last submitted "+DateHelper.retrieveTransmissionDate(Preferences.getLastUpdateTimeFromPrefs(getApplicationContext())));
        }
    }

}

