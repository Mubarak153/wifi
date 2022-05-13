package org.hdev.wifiwpspro;


public class Extra {


    public static String encryp_type(String encryp) {
        if (encryp.contains("WPA2")) {
            return "[WPA2]";
        }
        if (encryp.contains("WPA")) {
            return "[WPA]";
        }
        if (encryp.contains("WEP")) {
            return "[WEP]";
        }
        if (encryp.contains("ESS")) {
            return "[OPEN]";
        }
        return encryp;
    }


}


