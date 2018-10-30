package com.gk.sam;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

@TargetApi(Build.VERSION_CODES.M)
public final class PermissionManager {

    public static final int RESULT_PERMISSION_LOCATION = 1;
    private static final String[] PERMISSION_LOCATION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    public PermissionManager() {
        throw new AssertionError();
    }

    // Location Permissions
    public static boolean hasLocationPermissions(final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermissions(final Activity activity) {
        activity.requestPermissions(PERMISSION_LOCATION, RESULT_PERMISSION_LOCATION);
    }
}
