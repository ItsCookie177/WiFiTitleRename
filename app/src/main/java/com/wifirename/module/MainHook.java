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

    private static final String TAG = "WifiRename";
    private static final String[] TARGET_PACKAGES = {
            "com.android.settings",
            "com.android.systemui"
    };
    private static final String PRIMARY_CLASS = "com.android.wifitrackerlib.StandardWifiEntry";
    private static final String BASE_CLASS = "com.android.wifitrackerlib.WifiEntry";
    private static final String SCAN_PACKAGE = "com.android.wifitrackerlib";
    private static final String FALLBACK_SSID = "bagus.hanif";
    private static final String FALLBACK_LABEL = "Leo's wifi";

    private static final Object PREF_LOCK = new Object();
    private static final Map<String, String> MAPPING = new HashMap<>();
    private static XSharedPreferences xprefs;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        try {
            XposedBridge.log(TAG + ": loaded in " + lpparam.packageName);

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
            hookPrimary(lpparam.classLoader, hooked);
            if (hooked.isEmpty()) {
                tryHookBase(lpparam.classLoader, hooked);
            }
            if (hooked.isEmpty()) {
                scanAndHook(lpparam.classLoader, SCAN_PACKAGE, hooked);
            }
            if (hooked.isEmpty()) {
                XposedBridge.log(TAG + ": no WifiEntry class hooked in " + lpparam.packageName);
            }
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": handleLoadPackage(" + lpparam.packageName + ") failed: " + t);
        }
    }

    private static void hookPrimary(ClassLoader cl, List<Class<?>> hooked) {
        try {
            Class<?> clazz = cl.loadClass(PRIMARY_CLASS);
            XC_MethodHook titleHook = buildTitleHook();
            Set<XC_MethodHook.Unhook> unhooks = XposedBridge.hookAllMethods(clazz, "getTitle", titleHook);
            XposedBridge.log(TAG + ": hooked getTitle count=" + unhooks.size()
                    + " class=" + clazz.getName());
            if (unhooks.isEmpty()) {
                dumpTitleSsidMethods(clazz);
            } else {
                hooked.add(clazz);
            }
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": hookAllMethods(" + PRIMARY_CLASS + "#getTitle) failed: " + t);
        }
    }

    private static void tryHookBase(ClassLoader cl, List<Class<?>> hooked) {
        try {
            Class<?> clazz = cl.loadClass(BASE_CLASS);
            Method m = findConcreteGetTitle(clazz);
            if (m == null) {
                XposedBridge.log(TAG + ": " + BASE_CLASS + " has no hookable (non-abstract) getTitle()");
                return;
            }
            XposedBridge.hookMethod(m, buildTitleHook());
            hooked.add(clazz);
            XposedBridge.log(TAG + ": hooked base " + m.getDeclaringClass().getName() + "#getTitle");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": hooking base " + BASE_CLASS + " failed: " + t);
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
                try {
                    Class<?> clazz = cl.loadClass(name);
                    if (hooked.contains(clazz)) {
                        continue;
                    }
                    Method m = findConcreteGetTitle(clazz);
                    if (m == null) {
                        continue;
                    }
                    XposedBridge.hookMethod(m, buildTitleHook());
                    hooked.add(clazz);
                    XposedBridge.log(TAG + ": hooked (scan) " + clazz.getName() + "#getTitle");
                } catch (Throwable ignored) {
                }
            }
            XposedBridge.log(TAG + ": fallback scan done, hooked classes=" + hooked.size());
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": fallback class scan of " + pkg + " failed: " + t);
        }
    }

    private static XC_MethodHook buildTitleHook() {
        return new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    CharSequence orig = (CharSequence) param.getResult();
                    Map<String, String> map = refreshMapping();
                    XposedBridge.log(TAG + ": title=" + (orig == null ? "null" : orig.toString())
                            + " map=" + map.size());
                    if (orig == null) {
                        return;
                    }
                    String replacement = lookup(orig.toString(), map);
                    if (replacement != null && !replacement.contentEquals(orig)) {
                        param.setResult(replacement);
                        XposedBridge.log(TAG + ": replaced '" + orig + "' -> '" + replacement + "'");
                    }
                } catch (Throwable t) {
                    XposedBridge.log(TAG + ": getTitle hook error: " + t);
                }
            }
        };
    }

    private static void dumpTitleSsidMethods(Class<?> clazz) {
        try {
            for (Method m : clazz.getDeclaredMethods()) {
                String n = m.getName();
                if (n.contains("Title") || n.contains("Ssid") || n.contains("SSID")) {
                    XposedBridge.log(TAG + ": candidate method " + m + " abstract="
                            + Modifier.isAbstract(m.getModifiers()));
                }
            }
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": method dump failed: " + t);
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

    private static String lookup(String original, Map<String, String> map) {
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
            boolean readable = false;
            int size = -1;
            try {
                if (xprefs == null) {
                    xprefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, Prefs.PREFS_NAME);
                }
                File file = xprefs.getFile();
                readable = file != null && file.canRead();
                xprefs.reload();
                Map<String, ?> all = xprefs.getAll();
                size = all != null ? all.size() : -1;
                XposedBridge.log(TAG + ": prefs readable=" + readable + " size=" + size);

                Map<String, String> fresh = new HashMap<>();
                if (all != null) {
                    for (Map.Entry<String, ?> e : all.entrySet()) {
                        fresh.put(e.getKey(), String.valueOf(e.getValue()));
                    }
                }
                if (all != null && all.isEmpty() && file != null && file.exists()) {
                    fresh.putAll(parsePrefsXml(file));
                    XposedBridge.log(TAG + ": prefs xsp empty, direct xml parse size=" + fresh.size());
                }
                if (fresh.isEmpty()) {
                    fresh.put(FALLBACK_SSID, FALLBACK_LABEL);
                    XposedBridge.log(TAG + ": prefs empty/unreadable, using builtin fallback ("
                            + FALLBACK_SSID + " -> " + FALLBACK_LABEL + ")");
                }
                MAPPING.clear();
                MAPPING.putAll(fresh);
            } catch (Throwable t) {
                XposedBridge.log(TAG + ": prefs load failed, using builtin fallback: " + t);
                MAPPING.clear();
                MAPPING.put(FALLBACK_SSID, FALLBACK_LABEL);
            }
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
