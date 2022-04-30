package org.hdev.wifiwpspro;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.chainfire.libsuperuser.Shell.SH;
import eu.chainfire.libsuperuser.Shell.SU;

public class MainActivity extends AppCompatActivity {
    protected static boolean ScannAutomatique;
    protected static boolean premierOnCreate = true;
    protected static boolean locatParametreActivity = false;
    protected static boolean controlReciever;
    protected static boolean activatGPS = false;
    protected static boolean shouldUserRoot = true;
    protected static WifiManager wff;
    protected final Context context = this;
    private final int PERMISSIONS_REQUEST_LOCATION = 100;
    protected ArrayAdapter adapter;
    protected String prompte;
    protected boolean firstBooot = true;
    protected Intent intent;
    protected int derniereNet = 0;
    protected ListView list;
    protected ArrayList<Networking> networkingList;
    protected String pinCode;
    protected WifiReceiver receptorWifi;
    protected String selectedBSSID;
    protected String selectedESSID;
    protected String selectedPSK;
    protected boolean initialisationSYS = false;
    protected TextView noTextNet;
    protected String wpa_code_pin;
    private AdView mAdView;
    InterstitialAd mInterstitialAd;
    AdRequest adRequestint;

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
        if (premierOnCreate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
            setContentView(R.layout.activity_main_scanning);
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

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model.toUpperCase();
        }
        return manufacturer.toUpperCase() + " " + model;
    }

    public void shareApp() {
        try {
            Intent i = new Intent("android.intent.action.SEND");
            i.putExtra("android.intent.extra.SUBJECT", getString(R.string.app_name));
            i.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(i, "Choose one"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rateApp() {
        Intent goToMarket = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + getPackageName()));
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
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
            activatGPS = false;
            return;
        }
        activatGPS = true;
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0  || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_BACKGROUND_LOCATION") != 0) {
            Builder builder = new Builder(this);
            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.ACCESS_COARSE_LOCATION" , "android.permission.ACCESS_FINE_LOCATION" , "android.permission.ACCESS_BACKGROUND_LOCATION"}, PERMISSIONS_REQUEST_LOCATION);
                }
            });
            builder.setCancelable(false);
            builder.setMessage(R.string.request_gps_info);
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
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION"}, PERMISSIONS_REQUEST_LOCATION);
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
        setContentView(R.layout.activity_main_nouveu);
        settoolbar();
        loadAds();
        loadInterstitialAd();
        intent = new Intent().putExtra("List_Position", MODE_PRIVATE);
        wpa_code_pin = Extra.loadLib(context);
        networkingList = new ArrayList();
        NetAdapter adaptador = new NetAdapter(this, networkingList);
        adapter = adaptador;
        list = findViewById(R.id.list);
        list.setAdapter(adaptador);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Networking networking = (Networking) list.getItemAtPosition(position);
                if (derniereNet != position) {
                    intent.putExtra("List_Position", MODE_PRIVATE);
                    derniereNet = position;
                }
                if (networking.getINFO().contains("WPS")) {
                    showNetworkOptionsDialog(networking);
                } else {
                    Toast.makeText(getApplicationContext(), "This network does not have WPS enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
        noTextNet = findViewById(R.id.textNoNetworks);
        configWiFiReceiver();
    }

    private void loadInterstitialAd() {
        adRequestint = new AdRequest.Builder().build();
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_full_screen));
        mInterstitialAd.loadAd(adRequestint);
    }
    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void configWiFiReceiver() {
        wff = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        receptorWifi = new WifiReceiver();
        if (!wff.isWifiEnabled()) {
            Toast.makeText(this, R.string.enablingWiFi, Toast.LENGTH_SHORT).show();
            wff.setWifiEnabled(true);
        }
        ScannAutomatique = false;
        controlReciever = false;
        registerReceiver(receptorWifi, new IntentFilter("android.net.wifi.SCAN_RESULTS"));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void showScan() {
        if (wff == null) {
            configWiFiReceiver();
        }
        if (!wff.isWifiEnabled()) {
            Toast.makeText(this, R.string.enablingWiFi, Toast.LENGTH_SHORT).show();
            wff.setWifiEnabled(true);
        }
        controlReciever = true;
        wff.startScan();
        Toast.makeText(this, R.string.scanning, Toast.LENGTH_SHORT).show();
    }

    protected void NetInfo(List<ScanResult> results) {
        int i = 0;
        if (!ScannAutomatique) {
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

    private int getLock(String capabilities) {
        return (capabilities.contains("WPA2") || capabilities.contains("WPA") || capabilities.contains("WEP")) ? R.mipmap.ic_lock : R.mipmap.ic_lock_open;
    }

    private void loadAds() {
        //ADS
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principale, menu);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        }

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            showAbout();
        } else if (id == R.id.action_scan) {
            if (!ScannAutomatique) {
                if (!initialisationSYS) {
                    initialisationSYS = true;
                    loadSystem();
                }
                if (!activatGPS || isLocationEnabled(context)) {
                    intent.putExtra("List_Position", MODE_PRIVATE);
                    showScan();
                } else {
                    buildAlertMessageNoGps();
                }
            }
       // } else if (id == R.id.action_showpwd) {
           // startActivity(new Intent(this, MotdePasseActivity.class));
        } else if (id == R.id.action_pingen) {
            Intent i = new Intent(MainActivity.this, Generator.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    private void showNetworkOptionsDialog(Networking networking) {
        Builder builder = new Builder(context);
        selectedESSID = networking.getESSID();
        selectedBSSID = networking.getBSSID();
        String[] charSeq = Extra.calculePIN(networking);
        Log.d("Array List Values", String.valueOf(charSeq));
        final String BSSID = selectedBSSID;
        if (VERSION.SDK_INT >= 21) {
            View checkBoxView = View.inflate(this, R.layout.root_case_cocher, null);
            final CheckBox checkBox = checkBoxView.findViewById(R.id.caseaCocher);
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    shouldUserRoot = isChecked;
                    checkBox.setChecked(MainActivity.shouldUserRoot);
                    getPreferences(MODE_PRIVATE).edit().putBoolean("useRoot", shouldUserRoot).apply();
                }
            });
            checkBox.setText(getString(R.string.useRootMethod));
            boolean useRootSP = getPreferences(MODE_PRIVATE).getBoolean("useRoot", false);
            shouldUserRoot = useRootSP;
            checkBox.setChecked(useRootSP);
            builder.setView(checkBoxView);
        }
        List<String> your_array_list = new ArrayList();
        Collections.addAll(your_array_list, charSeq);
        builder.setSingleChoiceItems(new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, your_array_list), intent.getIntExtra("List_Position", 0), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                intent.putExtra("List_Position", which);
            }
        });
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                intent.putExtra("List_Position", 0);
            }
        });
        builder.setNeutralButton(R.string.alertDialog_trycustompin, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showCustomPINDialog(BSSID);
            }
        });
        final String[] finalCharSeq = charSeq;
        builder.setPositiveButton(R.string.alertDialog_trypin, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                pinCode = finalCharSeq[intent.getIntExtra("List_Position", 0)];
                prompte = wpa_code_pin + " IFNAME=wlan0 wps_reg " + BSSID + " " + pinCode;
                dialog.dismiss();
                showInterstitial();
                showChooseMethodDialog();
            }
        });
        builder.setCancelable(false);
        builder.setTitle(R.string.choosePIN);
        builder.create();
        builder.show();
    }

    private void showCustomPINDialog(final String BSSID) {
        Builder builder = new Builder(this.context);
        this.selectedBSSID = BSSID;
        final EditText custompin = new EditText(context);
        custompin.setInputType(InputType.TYPE_CLASS_NUMBER);
        custompin.setMaxLines(1);
        custompin.setFilters(new InputFilter[]{new LengthFilter(8)});
        builder.setView(custompin);
        builder.setTitle(R.string.lblInsertPIN);
        builder.create();
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.alertDialog_trypin, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                pinCode = custompin.getText().toString();
                prompte = wpa_code_pin + " IFNAME=wlan0 wps_reg " + BSSID + " " + pinCode;
                if (pinCode.length() != 8) {
                    Toast.makeText(context, getString(R.string.invalidPIN), Toast.LENGTH_SHORT).show();
                    showCustomPINDialog(selectedBSSID);
                    return;
                }
                dialog.dismiss();
                showChooseMethodDialog();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    protected void showChooseMethodDialog() {
        if (VERSION.SDK_INT < 21) {
            if (SU.available()) {
                new CallSU(prompte, selectedBSSID).execute();
            } else {
                showNoRootDeviceDialog();
            }
        } else if (!shouldUserRoot) {
            connectWithoutRoot(selectedBSSID, pinCode);
        } else if (SU.available()) {
            new CallSU(prompte, selectedBSSID).execute();
        } else {
            showNoRootDeviceDialog();
        }
    }

    protected boolean findPSK() {
        List<String> result = Extra.parseSupplicant();
        boolean haveSSID = false;
        selectedPSK = getString(R.string.no_available);
        for (String tmp : result) {
            if (!haveSSID && tmp.substring(1).contains(selectedESSID)) {
                haveSSID = true;
            } else if (haveSSID && tmp.substring(0, 1).equalsIgnoreCase("s")) {
                haveSSID = false;
            } else if (haveSSID && tmp.substring(0, 1).equalsIgnoreCase("p")) {
                selectedPSK = tmp.substring(1);
                return true;
            }
        }
        return false;
    }

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

    private void showSuccessDialog() {
        Builder builder = new Builder(this);
        builder.setTitle(getResources().getString(R.string.connected));
        builder.setMessage(getString(R.string.lblSSID) + " " + selectedESSID + "\n" + getString(R.string.lblPassword) + " " + selectedPSK + "\n\n" + getString(R.string.dialogSuccess));
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.copyClipBoard, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (selectedPSK.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.noInfoToClipBoard, Toast.LENGTH_SHORT).show();
                    return;
                }
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("", selectedPSK));
                Toast.makeText(getApplicationContext(), R.string.clipBoard, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    private void showFailDialog() {
        new Builder(this).setMessage(R.string.failDialogMsg).setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).create().show();
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
        if (ScannAutomatique) {
            wff.startScan();
        }
        if (locatParametreActivity) {
            if (isLocationEnabled(context)) {
                locatParametreActivity = false;
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

    private void buildAlertMessageNoGps() {
        new Builder(this).setMessage(R.string.dialog_need_active_gps_info).setCancelable(false).setPositiveButton((int) R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                locatParametreActivity = true;
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
            if (wff == null) {
                wff = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            }
            if (!wff.isWifiEnabled()) {
                Toast.makeText(this, R.string.enablingWiFi, Toast.LENGTH_SHORT).show();
                wff.setWifiEnabled(true);
            }
            final ConnectivityManager cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = cManager.getActiveNetworkInfo();
            if (mWifi != null && mWifi.getType() == 1 && mWifi.isConnected() && wff.getConnectionInfo().getBSSID().equalsIgnoreCase(BSSID)) {
                findPSK();
                showSuccessDialog();
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
                                    wff.cancelWps(null);
                                    dialog.dismiss();
                                }
                            });
                            progressDialog.show();
                        }
                    });
                }

                public void onSucceeded() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            NetworkInfo mWifi = cManager.getActiveNetworkInfo();
                            if (mWifi != null && mWifi.getType() == 1 && mWifi.isConnected() && !MainActivity.wff.getConnectionInfo().getBSSID().equalsIgnoreCase(selectedBSSID)) {
                                progressDialog.dismiss();
                                showFailDialog();
                            }
                            progressDialog.dismiss();
                            findPSK();
                            showSuccessDialog();
                        }
                    });
                }

                public void onFailed(int reason) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            showFailDialog();
                        }
                    });
                }
            };
            WpsInfo wpsInfo = new WpsInfo();
            wpsInfo.setup = 2;
            wpsInfo.pin = pin;
            wpsInfo.BSSID = BSSID;
            wff.startWps(wpsInfo, wpsCallback);
        }
    }

    private class CallSU extends AsyncTask<Void, Integer, Integer> {
        static final int CONNECTED = 1;
        static final int NOROOTDEVICE = -1;
        static final int NOT_CONNECTED = 0;
        final String BSSID;
        final String cmd;
        final ProgressDialog pDialog;

        public CallSU(String cmd, String BSSID) {
            this.pDialog = new ProgressDialog(context);
            this.cmd = cmd;
            this.BSSID = BSSID;
        }

        protected Integer doInBackground(Void... params) {
            ConnectivityManager cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWifi.isConnected()) {
                if (wff.getConnectionInfo().getBSSID().equalsIgnoreCase(BSSID)) {
                    findPSK();
                    return CONNECTED;
                }
                wff.disconnect();
                while (mWifi.isConnected()) {
                    mWifi = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                }
            }
            Log.e("COMANDO", cmd);
            if (!SU.available()) {
                return NOROOTDEVICE;
            }
            List<String> resultArray = SU.run(cmd);
            if (resultArray == null || resultArray.isEmpty() || !(resultArray.isEmpty() || (resultArray.get(0)).contains("OK"))) {
                SU.run(cmd.replace("IFNAME=wlan0 ", ""));
                Log.e("COMANDO 2", cmd.replace("IFNAME=wlan0 ", ""));
            }
            long time = System.currentTimeMillis();
            boolean out = false;
            while (!out && System.currentTimeMillis() < 10000 + time) {
                if (cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() && wff.getConnectionInfo().getBSSID().equalsIgnoreCase(BSSID)) {
                    out = true;
                }
            }
            return findPSK() ? CONNECTED : NOT_CONNECTED;
        }

        protected void onProgressUpdate(Integer... values) {
            pDialog.setProgress(values[0]);
        }

        protected void onPreExecute() {
            if (wff == null) {
                wff = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            }
            if (!wff.isWifiEnabled()) {
                Toast.makeText(MainActivity.this, getString(R.string.enablingWiFi), Toast.LENGTH_SHORT).show();
                wff.setWifiEnabled(true);
            }
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setMessage(getResources().getString(R.string.tryConnection));
            pDialog.setMax(1);
            pDialog.setProgress(0);
            pDialog.setCancelable(false);
            pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            pDialog.show();
        }

        protected void onPostExecute(Integer control) {
            pDialog.dismiss();
            switch (control) {
                case NOROOTDEVICE:
                    showNoRootDeviceDialog();
                    return;
                case NOT_CONNECTED:
                    showFailDialog();
                    return;
                case CONNECTED:
                    showSuccessDialog();
                    return;
                default:
                    showFailDialog();
            }
        }
    }

    private class WifiReceiver extends BroadcastReceiver {
        private WifiReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            if (ScannAutomatique || controlReciever) {
                NetInfo(wff.getScanResults());
            }
        }
    }
}
