package org.hdev.wifiwpspro;


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


}


