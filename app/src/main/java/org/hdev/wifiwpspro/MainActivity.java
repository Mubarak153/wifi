package org.hdev.wifiwpspro;

import android.annotation.TargetApi;
import android.app.ProgressDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WpsCallback;
import android.net.wifi.WpsInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell.SH;

public class MainActivity extends AppCompatActivity {
    protected static boolean scanauto;
    protected static boolean firstversion = true;
    protected static boolean locationactvity = false;
    protected static boolean controlReciever;
    protected static boolean activateGPS = false;
    protected static WifiManager wifi_info;
    protected final Context context = this;
    private final int PERMISSIONS_REQUEST_LOCATION = 100;
    protected ArrayAdapter adapter;
    protected boolean firstBooot = true;
    protected Intent intent;
    protected int latestver = 0;
    protected ListView list;
    protected ArrayList<Networking> networkingList;
    protected WifiReceiver receptorWifi;
    protected String selectedBSSID;
    protected boolean initialisationSYS = false;
    protected TextView noTextNet;

//Checking version for lcocation services
    private static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        if (VERSION.SDK_INT >= 19) {
            try {
                locationMode = Secure.getInt(context.getContentResolver(), "location_mode");
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != 0;
        } else
            return !TextUtils.isEmpty(Secure.getString(context.getContentResolver(), "location_providers_allowed"));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (firstversion) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
            //Welcome message
            setContentView(R.layout.scanning_page);
            settoolbar();
            new LovelyInfoDialog(this)
                    .setTopColorRes(R.color.colorAccent)
                    .setIcon(R.drawable.ic_info_black_24dp)
                    .setNotShowAgainOptionEnabled(0)
                    .setNotShowAgainOptionChecked(true)
                    .setTitle(R.string.info_title)
                    .setMessage(R.string.info_message)
                    .show();
        }
        firstBooot = getPreferences(MODE_PRIVATE).getBoolean("firstAppBoot", true);
        if (firstBooot) {
            getPreferences(MODE_PRIVATE).edit().putBoolean("useRoot", false).apply();
        }
        showPermissionRequestInfo();
    }
//toolbar configurations
    private void settoolbar() {
        RelativeLayout layout = findViewById(R.id.rellor);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColors(new int[]{
                Color.parseColor("#328cf1"),
                Color.parseColor("#52b4ff")
        });

        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        layout.setBackground(drawable);

        //Toolbar
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
    }


    private void showInfoAboutGPSBelowAndroidM() {
        if (this.firstBooot) {
            new Builder(this).setPositiveButton(R.string.ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setCancelable(false).setMessage(R.string.info_gps).create().show();
        }
    }

    private void showPermissionRequestInfo() {
        if (VERSION.SDK_INT < 23) {
            showInfoAboutGPSBelowAndroidM();
            activateGPS = false;
            return;
        }
        activateGPS = true;
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_BACKGROUND_LOCATION") != 0) {
            Builder builder = new Builder(this);
            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION"}, PERMISSIONS_REQUEST_LOCATION);
                }
            });
            builder.setCancelable(false);
            builder.setMessage(R.string.welcome_mes);
            AlertDialog dialog = builder.create();
            dialog.show();
            ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void showPermissionDeniedInfo() {
        Builder builder = new Builder(this);
        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION"}, PERMISSIONS_REQUEST_LOCATION);
            }
        });
        builder.setCancelable(false);
        builder.setMessage(R.string.request_gps_denied_info);
        builder.create().show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    showPermissionDeniedInfo();
                    return;
                }
                if (!initialisationSYS) {
                    initialisationSYS = true;
                    loadSystem();
                }
                if (isLocationEnabled(context) || VERSION.SDK_INT < 23) {
                    intent.putExtra("List_Position", 0);
                    showScan();
                    return;
                }
                buildAlertMessageNoGps();
                return;
            default:
        }
    }

    private void loadSystem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.home_page2);
        settoolbar();


        intent = new Intent().putExtra("List_Position", MODE_PRIVATE);

        networkingList = new ArrayList();
        NetAdapter adaptador = new NetAdapter(this, networkingList);
        adapter = adaptador;
        list = findViewById(R.id.list);
        list.setAdapter(adaptador);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Networking networking = (Networking) list.getItemAtPosition(position);
                if (latestver != position) {
                    intent.putExtra("List_Position", MODE_PRIVATE);
                    latestver = position;
                }
                if (networking.getINFO().contains("WPS")) {

                } else {
                    Toast.makeText(getApplicationContext(), "This network does not have WPS enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
        noTextNet = findViewById(R.id.textNoNetworks);
        configWiFiReceiver();
    }


    private void configWiFiReceiver() {
        wifi_info = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        receptorWifi = new WifiReceiver();
        if (!wifi_info.isWifiEnabled()) {
            Toast.makeText(this, R.string.enablingWiFi, Toast.LENGTH_SHORT).show();
            wifi_info.setWifiEnabled(true);
        }
        scanauto = false;
        controlReciever = false;
        registerReceiver(receptorWifi, new IntentFilter("android.net.wifi.SCAN_RESULTS"));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void showScan() {
        if (wifi_info == null) {
            configWiFiReceiver();
        }
        if (!wifi_info.isWifiEnabled()) {
            Toast.makeText(this, R.string.enablingWiFi, Toast.LENGTH_SHORT).show();
            wifi_info.setWifiEnabled(true);
        }
        controlReciever = true;
        wifi_info.startScan();
        Toast.makeText(this, R.string.scanning, Toast.LENGTH_SHORT).show();
    }

    protected void NetInfo(List<ScanResult> results) {
        int i = 0;
        if (!scanauto) {
            controlReciever = false;
        }
        if (results != null) {
            if (networkingList == null) {
                networkingList = new ArrayList();
            } else {
                networkingList.clear();
            }
            List<ScanResult> tmp = new ArrayList();
            for (ScanResult net : results) {
                tmp.add(net);
            }
            this.noTextNet.setText(R.string.mainNoNetworks);
            TextView textView = this.noTextNet;
            if (!tmp.isEmpty()) {
                i = 8;
            }
            textView.setVisibility(i);
            while (!tmp.isEmpty()) {
                int minLevel = -1000;
                int position = -1;
                for (short i2 = (short) 0; i2 < tmp.size(); i2 = (short) (i2 + 1)) {
                    int currentLevel = (tmp.get(i2)).level;
                    if (minLevel < currentLevel) {
                        minLevel = currentLevel;
                        position = i2;
                    }
                }
                networkingList.add(new Networking((tmp.get(position)).BSSID.toUpperCase(), (tmp.get(position)).SSID, (tmp.get(position)).capabilities, String.valueOf((tmp.get(position)).level), getWiFi((tmp.get(position)).level), getLock((tmp.get(position)).capabilities)));
                tmp.remove(position);
            }
            adapter.notifyDataSetChanged();
        }
    }
//encryption type
    private int getLock(String capabilities) {
        return (capabilities.contains("WPA2") || capabilities.contains("WPA") || capabilities.contains("WEP")) ? R.mipmap.ic_lock : R.mipmap.ic_lock_open;
    }

//The Received Signal Strength Indicator (RSSI) is an estimation of a device's ability to hear, detect, and receive signals from any wireless access point or Wi-Fi router.
    private int getWiFi(int rssi) {
        switch (WifiManager.calculateSignalLevel(rssi, 4)) {
            case 0:
                return R.mipmap.ic_wifi_1;
            case 1:
                return R.mipmap.ic_wifi_2;
            case 2:
                return R.mipmap.ic_wifi_3;
            case 3:
                return R.mipmap.ic_wifi_4;
            default:
                return -1;
        }
    }
//scan button
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_page, menu);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        }

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }



//Root option

    private void showNoRootDeviceDialog() {
        String model = (SH.run("getprop ro.product.model").get(0)).replace(" ", "+");
        String brand = SH.run("getprop ro.product.brand").get(0);
        AlertDialog dialog = new Builder(this).setMessage(Html.fromHtml(String.format(getString(R.string.noRootInfo), new Object[]{model, brand}))).setNegativeButton((int) R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).create();
        dialog.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }




    protected void onPause() {
        if (receptorWifi != null) {
            unregisterReceiver(receptorWifi);
        }
        Log.e("APP", "onPause");
        super.onPause();
    }

    protected void onResume() {
        if (intent == null) {
            intent = new Intent().putExtra("List_Position", 0);
        }
        if (receptorWifi != null) {
            registerReceiver(receptorWifi, new IntentFilter("android.net.wifi.SCAN_RESULTS"));
        }
        if (scanauto) {
            wifi_info.startScan();
        }
        if (locationactvity) {
            if (isLocationEnabled(context)) {
                locationactvity = false;
                showScan();
            } else {
                buildAlertMessageNoGps();
            }
        }
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
        if (this.firstBooot) {
            getPreferences(MODE_PRIVATE).edit().putBoolean("firstAppBoot", false).apply();
        }
    }
// Message if location services weren't enabled
    private void buildAlertMessageNoGps() {
        new Builder(this).setMessage(R.string.dialog_need_active_gps_info).setCancelable(false).setPositiveButton((int) R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                locationactvity = true;
            }
        }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                noTextNet.setText(R.string.dialog_gps_cancel);
                noTextNet.setVisibility(View.VISIBLE);
                dialog.cancel();
            }
        }).create().show();
    }

    @TargetApi(21)
    private void connectWithoutRoot(String BSSID, String pin) {
        if (BSSID != null && pin != null) {
            if (wifi_info == null) {
                wifi_info = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            }
            if (!wifi_info.isWifiEnabled()) {
                Toast.makeText(this, R.string.enablingWiFi, Toast.LENGTH_SHORT).show();
                wifi_info.setWifiEnabled(true);
            }
            final ConnectivityManager cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = cManager.getActiveNetworkInfo();
            if (mWifi != null && mWifi.getType() == 1 && mWifi.isConnected() && wifi_info.getConnectionInfo().getBSSID().equalsIgnoreCase(BSSID)) {
                return;
            }
            final ProgressDialog progressDialog = new ProgressDialog(context);
            WpsCallback wpsCallback = new WpsCallback() {
                public void onStarted(String pin) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setMessage(getResources().getString(R.string.tryConnection));
                            progressDialog.setMax(1);
                            progressDialog.setProgress(0);
                            progressDialog.setCancelable(false);
                            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    wifi_info.cancelWps(null);
                                    dialog.dismiss();
                                }
                            });
                            progressDialog.show();
                        }
                    });
                }
//
                public void onSucceeded() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            NetworkInfo mWifi = cManager.getActiveNetworkInfo();
                            if (mWifi != null && mWifi.getType() == 1 && mWifi.isConnected() && !MainActivity.wifi_info.getConnectionInfo().getBSSID().equalsIgnoreCase(selectedBSSID)) {
                                progressDialog.dismiss();

                            }
                            progressDialog.dismiss();

                        }
                    });
                }

                public void onFailed(int reason) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();

                        }
                    });
                }
            };
            WpsInfo wpsInfo = new WpsInfo();
            wpsInfo.setup = 2;
            wpsInfo.BSSID = BSSID;
            wifi_info.startWps(wpsInfo, wpsCallback);
        }
    }

    private class WifiReceiver extends BroadcastReceiver {
        private WifiReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            if (scanauto || controlReciever) {
                NetInfo(wifi_info.getScanResults());
            }
        }
    }
}
