package com.example.platypus.kitkat_storage_poc.util;

import android.Manifest;
import android.app.Activity;

import androidx.core.app.ActivityCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionUtil {

    public static boolean requestExternalStoragePermission(Activity activity, int permissionRequestCode) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionRequestCode);
            return false;
        }
    }
}
