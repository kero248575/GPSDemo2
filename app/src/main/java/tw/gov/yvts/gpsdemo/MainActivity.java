package tw.gov.yvts.gpsdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LocationListener {
    static final int MIN_TIME = 5000;
    static final float MIN_DIST = 5;
    LocationManager mgr;
    TextView tv1, tv2, tvshow;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    EditText edt1, edt2;
    Geocoder geocoder;
    Location myLocation;
    String straddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1 = (TextView) findViewById(R.id.textview1);
        tv2 = (TextView) findViewById(R.id.textView2);
        tvshow = (TextView) findViewById(R.id.textView4);
        edt1 = (EditText) findViewById(R.id.editText);
        edt2 = (EditText) findViewById(R.id.editText2);
        geocoder = new Geocoder(this, Locale.getDefault());
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkPermission();
    }

    // ===================
    @Override
    protected void onResume() {

        super.onResume();
        tv1.setText("尚未取得定位資訊");
        enableLocationUpdates(true);
        String str = "GPS定位: " + (isGPSEnabled ? "開啟" : "關閉");
        str += "\n網路定位: " + (isNetworkEnabled ? "開啟" : "關閉");
        tv1.setText(str);
    }

    // ======================
    @Override
    protected void onPause() {
        super.onPause();
        enableLocationUpdates(false);
    }

    // ========================
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length >= 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "程式需要定位權限才能運作", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ========================
    private void enableLocationUpdates(boolean isTurnOn) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (isTurnOn) {
                isGPSEnabled = mgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (!isGPSEnabled && !isNetworkEnabled) {
                    Toast.makeText(this, "請確認已開啟定位功能", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "取得定位資訊中...", Toast.LENGTH_SHORT).show();
                    if (isGPSEnabled) {
                        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this);
                    }
                    if (isNetworkEnabled) {
                        mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, this);
                    }
                }
            } else {
                mgr.removeUpdates(this);
            }
        }
    }

    // ========================
    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        String str = "定位提供者: " + location.getProvider();
        //str += String.format("\n緯度:%.5f\n經度:%.5f\n高度:%.2f公尺", location.getLatitude(), location.getLongitude(), location.getAltitude());
        str += String.format("\n緯度:%s\n經度:%s\n高度:%.2f公尺", Location.convert(location.getLatitude(), Location.FORMAT_SECONDS)
                , Location.convert(location.getLongitude(), Location.FORMAT_SECONDS), location.getAltitude());
        tv2.setText(str);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    // ====================
    public void setup(View v) {
        Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(it);
    }

    // ==========================
    public void getLocation(View v) {
        if (myLocation != null) {
            edt1.setText(Double.toString(myLocation.getLatitude()));
            edt2.setText(Double.toString(myLocation.getLongitude()));
        } else {
            tvshow.setText("No Data display");
        }

    }

    // =========================
    public void onQuery(View v) {
        String strlat = edt1.getText().toString();
        String strlon = edt2.getText().toString();

        if (strlat.length() == 0 || strlon.length() == 0) {
            return;
        } else {
            double latitue = Double.parseDouble(strlat);
            double lontitue = Double.parseDouble(strlon);
            tvshow.setText("");
            try {
                List<Address> listaddr = geocoder.getFromLocation(latitue, lontitue, 1);
                if (listaddr == null || listaddr.size() == 0) {
                    straddress += "ERROR ADDRESS";
                } else {
                    Address address = listaddr.get(0);
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        straddress += address.getAddressLine(i) + "\n";
                    }
                }


            } catch (Exception e) {
                straddress += "ERROR ADDRESS    " + e.toString();
            }

            tvshow.setText(straddress);
        }


    }
}

