package com.wifirename.module;

import android.content.Context;

import java.io.File;

public final class Prefs {

    public static final String PREFS_NAME = "ssid_map";

    private Prefs() {
    }

    public static void makeWorldReadable(Context context) {
        try {
            String dirPath = context.getApplicationInfo().dataDir;
            if (dirPath == null) {
                return;
            }
            File dataDir = new File(dirPath);
            if (!dataDir.exists()) {
                return;
            }
            File prefsDir = new File(dataDir, "shared_prefs");
            File prefsFile = new File(prefsDir, PREFS_NAME + ".xml");
            dataDir.setExecutable(true, false);
            prefsDir.setExecutable(true, false);
            if (prefsFile.exists()) {
                prefsFile.setReadable(true, false);
            }
        } catch (Throwable ignored) {
        }
    }
}
