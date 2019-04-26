package reen.com.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    String TAG = "WEATHER";
    @BindView(R.id.texttimezone)
    TextView txttimezone;

    @BindView(R.id.texttime)
    TextView txttime;

    @BindView(R.id.textsummary)
    TextView txtsummary;

    @BindView(R.id.texttemprature)
    TextView txttemperature;

    @BindView(R.id.textpressure)
    TextView txtpressure;

    @BindView(R.id.texthumidity)
    TextView txthumidity;

    @BindView(R.id.textwindspeed)
    TextView txtwindspeed;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.textlati)
    TextView txtlati;

    @BindView(R.id.textlongi)
    TextView txtlongi;

    double latitude=0.0;
    double longitude=0.0;


    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();
        fetchGPS();

    }

    @OnClick(R.id.txtfetch)
    public void fetch() {
        String url = "https://api.darksky.net/forecast/769c32fce791b956c9274a8d40ab0e9c/"+latitude +","+longitude;
        Log.d(TAG, "fetch: "+url );

        AsyncHttpClient client = new AsyncHttpClient();

        progressBar.setVisibility(View.VISIBLE);


        client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(MainActivity.this, "failed try again", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: " + responseString);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onSuccess:" + responseString);
                Log.i(TAG, "onSuccess: fetched weather data");

                try {
                    JSONObject mainobject = new JSONObject(responseString);
                    String timezone = mainobject.getString("timezone");

                    long time = mainobject.getJSONObject("currently").getLong("time");

                    String now = timeconverter(time);

                    String summery = mainobject.getJSONObject("currently").getString("summary");

                    double tempereture = mainobject.getJSONObject("currently").getDouble("temperature");

                    double pressure = mainobject.getJSONObject("currently").getDouble("pressure");
                    double humidity = mainobject.getJSONObject("currently").getDouble("humidity");
                    double windspeed = mainobject.getJSONObject("currently").getDouble("windSpeed");

                    Log.d(TAG, "onSuccess: time zone is" + timezone);
                    Log.d(TAG, "onSuccess: time is" + time);
                    Log.d(TAG, "onSuccess: summary is" + summery);
                    Log.d(TAG, "onSuccess: temperature is" + tempereture);
                    Log.d(TAG, "onSuccess: preasure is" + pressure);
                    Log.d(TAG, "onSuccess: humidity is" + humidity);
                    Log.d(TAG, "onSuccess: windspeed is" + windspeed);

                    txttimezone.setText(timezone);
                    txttime.setText(now);
                    txthumidity.setText(humidity + "");
                    txttemperature.setText(tempereture + "");
                    txtwindspeed.setText(windspeed + "");
                    txtsummary.setText(summery);
                    txtpressure.setText(pressure + "");


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "error with json", Toast.LENGTH_SHORT).show();


                }

            }
        });

    }

    public String timeconverter(long timeinseconds) {
        Date d = new Date(timeinseconds * 1000);
        DateFormat formatter = new SimpleDateFormat("HH:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone("africa/nairobi"));
        String time = formatter.format(d);
        return time;

    }

    public void fetchGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            double lat=location.getLatitude();
                            double lon=location.getLongitude();
                            txtlati.setText(lat+"");
                            txtlongi.setText(lon+"");

                            latitude=lat;
                            longitude=lon;
                        }
                    }
                });

    }

    static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=2000;
    boolean mLocationPermissionGranted=false;

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    //getDeviceLocation();
                    fetchGPS();
                }
            }
        }
        //  updateLocationUI();
    }

}
