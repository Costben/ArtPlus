package dev.artplus.mobile;

import android.content.res.Configuration;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class UxIconConfigCli {
    private UxIconConfigCli() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("usage: UxIconConfigCli <uxIconConfigLong>");
        }
        long value = Long.decode(args[0]);
        Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
        Object activityManager = activityManagerClass.getMethod("getService").invoke(null);
        Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");
        Method getConfiguration = iActivityManagerClass.getMethod("getConfiguration");
        Method updateConfiguration = iActivityManagerClass.getMethod(
            "updateConfiguration",
            Configuration.class
        );
        Configuration configuration = (Configuration) getConfiguration.invoke(activityManager);
        Object extra = getOplusExtraConfiguration(configuration);
        setLong(extra, "mUxIconConfig", value);
        incrementInt(extra, "mThemeChanged");
        updateConfiguration.invoke(activityManager, configuration);
        System.out.println("mUxIconConfig=" + Long.toHexString(value));
    }

    private static Object getOplusExtraConfiguration(Configuration configuration) throws Exception {
        Method getter = configuration.getClass().getMethod("getOplusExtraConfiguration");
        return getter.invoke(configuration);
    }

    private static void setLong(Object target, String fieldName, long value) throws Exception {
        Field field = target.getClass().getField(fieldName);
        field.setLong(target, value);
    }

    private static void incrementInt(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getField(fieldName);
        field.setInt(target, field.getInt(target) + 1);
    }
}
