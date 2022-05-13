package org.hdev.wifiwpspro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;
import androidx.core.view.ViewCompat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell.SH;
import eu.chainfire.libsuperuser.Shell.SU;

public class Extra {

    public static String capabilitiesTypeResume(String capabilities) {
        if (capabilities.contains("WPA2")) {
            return "[WPA2]";
        }
        if (capabilities.contains("WPA")) {
            return "[WPA]";
        }
        if (capabilities.contains("WEP")) {
            return "[WEP]";
        }
        if (capabilities.contains("ESS")) {
            return "[OPEN]";
        }
        return capabilities;
    }

    private static int fragmentBSSID(String bssid) {
        String[] splitBSSID = bssid.split(":");
        return Integer.valueOf(splitBSSID[0] + splitBSSID[1] + splitBSSID[2], 16);
    }

    private static int secondFragmentBSSID(String bssid) {
        String[] splitBSSID = bssid.split(":");
        return Integer.valueOf(splitBSSID[3] + splitBSSID[4] + splitBSSID[5], 16);
    }


}


