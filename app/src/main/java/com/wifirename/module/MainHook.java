package com.wifirename.module;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dalvik.system.DexFile;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public final class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "WiFiTitleRename";
    private static final String[] TARGET_PACKAGES = {
            "com.android.settings",
            "com.android.systemui"
    };
    private static final String[] PRIMARY_CANDIDATES = {
            "com.android.wifitrackerlib.StandardWifiEntry"
    };
    private static final String SCAN_PACKAGE = "com.android.wifitrackerlib";

    private static final Object PREF_LOCK = new Object();
    private static final Map<String, String> MAPPING = new HashMap<>();
    private static XSharedPreferences xprefs;
    private static long cacheStamp = -1;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        try {
            boolean target = false;
            for (String pkg : TARGET_PACKAGES) {
                if (pkg.equals(lpparam.packageName)) {
                    target = true;
                    break;
                }
            }
            if (!target) {
                return;
            }

            List<Class<?>> hooked = new ArrayList<>();
            for (String name : PRIMARY_CANDIDATES) {
                hookClass(lpparam.classLoader, name, hooked, true);
            }
            if (hooked.isEmpty()) {
                scanAndHook(lpparam.classLoader, SCAN_PACKAGE, hooked);
            }
            if (hooked.isEmpty()) {
                XposedBridge.log(TAG + ": no WifiEntry class hooked in " + lpparam.packageName
                        + "; StandardWifiEntry/getTitle not found (HyperOS modified?)");
            }
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": handleLoadPackage(" + lpparam.packageName + ") failed: " + t);
        }
    }

    private static void hookClass(ClassLoader cl, String className, List<Class<?>> hooked, boolean verbose) {
        try {
            Class<?> clazz = cl.loadClass(className);
            if (hooked.contains(clazz)) {
                return;
            }
            Method m = findConcreteGetTitle(clazz);
            if (m == null) {
                if (verbose) {
                    XposedBridge.log(TAG + ": " + className + " has no hookable (non-abstract) getTitle()");
                }
                return;
            }
            XposedBridge.hookMethod(m, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        CharSequence orig = (CharSequence) param.getResult();
                        if (orig == null) {
                            return;
                        }
                        String replacement = lookup(orig.toString());
                        if (replacement != null && !replacement.contentEquals(orig)) {
                            param.setResult(replacement);
                        }
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + ": getTitle replacement failed: " + t);
                    }
                }
            });
            hooked.add(clazz);
            XposedBridge.log(TAG + ": hooked " + m.getDeclaringClass().getName() + "#getTitle (target=" + className + ")");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": hooking " + className + " failed: " + t);
        }
    }

    private static void scanAndHook(ClassLoader cl, String pkg, List<Class<?>> hooked) {
        try {
            Set<String> names = new HashSet<>();
            Object pathList = XposedHelpers.getObjectField(cl, "pathList");
            Object[] elements = (Object[]) XposedHelpers.getObjectField(pathList, "dexElements");
            for (Object element : elements) {
                try {
                    Object dex = XposedHelpers.getObjectField(element, "dexFile");
                    if (!(dex instanceof DexFile)) {
                        continue;
                    }
                    Enumeration<String> entries = ((DexFile) dex).entries();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement();
                        if (name != null && name.startsWith(pkg)) {
                            names.add(name);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
            for (String name : names) {
                hookClass(cl, name, hooked, false);
            }
            XposedBridge.log(TAG + ": fallback scan done, hooked classes=" + hooked.size());
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": fallback class scan of " + pkg + " failed: " + t);
        }
    }

    private static Method findConcreteGetTitle(Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        for (Method m : clazz.getDeclaredMethods()) {
            if ("getTitle".equals(m.getName())
                    && m.getParameterCount() == 0
                    && !Modifier.isAbstract(m.getModifiers())) {
                return m;
            }
        }
        return null;
    }

    private static String lookup(String original) {
        Map<String, String> map = refreshMapping();
        if (map.isEmpty()) {
            return null;
        }
        String v = map.get(original);
        if (v == null) {
            String unquoted = stripQuotes(original);
            if (!unquoted.equals(original)) {
                v = map.get(unquoted);
            }
            if (v == null) {
                v = map.get("\"" + unquoted + "\"");
            }
        }
        if (v != null && v.isEmpty()) {
            return null;
        }
        return v;
    }

    private static String stripQuotes(String s) {
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static Map<String, String> refreshMapping() {
        synchronized (PREF_LOCK) {
            if (xprefs == null) {
                try {
                    xprefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, Prefs.PREFS_NAME);
                } catch (Throwable t) {
                    xprefs = null;
                }
            }
            File file = xprefs != null ? xprefs.getFile() : null;
            if (file == null) {
                file = new File("/data/user/0/" + BuildConfig.APPLICATION_ID
                        + "/shared_prefs/" + Prefs.PREFS_NAME + ".xml");
                if (!file.exists()) {
                    file = new File("/data/data/" + BuildConfig.APPLICATION_ID
                            + "/shared_prefs/" + Prefs.PREFS_NAME + ".xml");
                }
            }
            if (!file.exists()) {
                MAPPING.clear();
                cacheStamp = -1;
                return MAPPING;
            }
            long stamp = file.lastModified() + file.length();
            if (stamp == cacheStamp) {
                return MAPPING;
            }
            cacheStamp = stamp;

            Map<String, String> fresh = new HashMap<>();
            if (xprefs != null) {
                try {
                    xprefs.reload();
                    Map<String, ?> all = xprefs.getAll();
                    if (all != null) {
                        for (Map.Entry<String, ?> e : all.entrySet()) {
                            fresh.put(e.getKey(), String.valueOf(e.getValue()));
                        }
                    }
                } catch (Throwable t) {
                    XposedBridge.log(TAG + ": XSharedPreferences reload failed: " + t);
                }
            }
            if (fresh.isEmpty()) {
                fresh.putAll(parsePrefsXml(file));
            }
            MAPPING.clear();
            MAPPING.putAll(fresh);
            return MAPPING;
        }
    }

    private static Map<String, String> parsePrefsXml(File file) {
        Map<String, String> out = new HashMap<>();
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            int type = parser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                if (type == XmlPullParser.START_TAG && "string".equals(parser.getName())) {
                    String key = parser.getAttributeValue(null, "name");
                    String value = parser.nextText();
                    if (key != null) {
                        out.put(key, value);
                    }
                }
                type = parser.next();
            }
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": prefs xml fallback parse failed: " + t);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Throwable ignored) {
            }
        }
        return out;
    }
}
