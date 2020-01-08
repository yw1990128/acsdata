package com.skydigital.acsdatamanager;

import android.content.Context;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class UsbManager {
    private static final String TAG = "yw-test";
    private static Context mContext = null;
    private static String mExternalStoragePath = null;
    private static final String mKeyFilePathSuffix = "/update2.zip";
    private static final int BUFFER_PATHS_NUM = 20;
    private static List<String> mMountPathDataList = new ArrayList<String>();

    public static void setContext(Context context) {
        mContext = context;
        mExternalStoragePath = "/storage";
    }

    public static String checkUsbFileExist() {
        String keyFileFullPath;

        // 1. check usb mount
        if (getUsbMountedNum() <= 0) {
            Toast.makeText(mContext, "Failed to check usb device.", Toast.LENGTH_LONG).show();
            return null;
        }

        // 2. check otapackage exist
        String[] usbPathList = getUsbPaths();

        if (null == usbPathList) {
            Log.d(TAG, "checkUsbFileExist -> Failed to get usb path list, null.");
            return null;
        }

        if (0 == usbPathList.length) {
            Log.d(TAG, "checkUsbFileExist -> Failed to get usb path list, len is 0.");
            return null;
        }

        for (String path : usbPathList) {
            Log.d(TAG, "checkUsbFileExist -> path: " + path);
            keyFileFullPath = path + mKeyFilePathSuffix;
            Log.d(TAG, "checkUsbFileExist -> keyFileFullPath: " + keyFileFullPath);
            File file = new File(keyFileFullPath);
            Log.d(TAG, "checkUsbFileExist -> file: " + file);
            if (file.exists()) {
                Log.d(TAG, "checkUsbFileExist -> Success to find the key file " + keyFileFullPath);
                int index = keyFileFullPath.lastIndexOf("/");
                String before = keyFileFullPath.substring(0, index + 1);
                Log.d(TAG, "checkUsbFileExist -> Success to find the key folder " + before);
                return before;
            }
        }

        return null;
    }

    private static int getUsbMountedNum() {
        String[] deviceMountedList = null;
        deviceMountedList = readDevicePaths(mExternalStoragePath);
        if (null == deviceMountedList) {
            return 0;
        }

        return deviceMountedList.length;
    }

    private static boolean isSpecialFile(String file) {
        if (file.startsWith("/storage/null")) {
            return true;
        }

        if (file.startsWith("/storage/emulated")) {
            return true;
        }

        //BEGIN. MODIFY BY TCH.
        if (file.startsWith("/storage/sdcard")) {
            return true;
        }
        //FINISH. MODIFY BY TCH.

        return false;
    }

    private static boolean isPathInList(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        for (String data : mMountPathDataList) {
            if (TextUtils.isEmpty(data)) {
                continue;
            }

            if (data.equals(path)) {
                return true;
            }
        }

        return false;
    }

    private static boolean checkMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        return true;
    }

    private static String[] getUsbPaths() {
        String[] usbPaths = null;
        String tempPaths = null;
        usbPaths = readDevicePaths(mExternalStoragePath);
        return usbPaths;
    }

    private static String[] getVolumePaths(StorageManager sm) {
        String[] paths = null;

        try {
            Method method = StorageManager.class.getMethod("getVolumePaths", new Class<?>[0]);
            method.setAccessible(true);
            try {
                Log.d(TAG, "method = " + method.toString());
                paths = (String[])method.invoke(sm, new Object[0]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        for (int i = 0; i< paths.length; i++) {
            Log.d(TAG, "paths = " + paths[i]);
        }

        return paths;
    }

    private static String[] readDevicePaths(String prefix) {
        String[] paths = null;
        String[] devicePaths = new String[BUFFER_PATHS_NUM];
        int index = 0;

        StorageManager storageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        //BEGIN. MODIFY BY TCH.
        //paths = storageManager.getVolumePaths();
        paths = getVolumePaths(storageManager);
        //FINISH. MODIFY BY TCH.
        if (null == paths) {
            return null;
        }

        for (String tempPath : paths) {
            if (tempPath==null|| TextUtils.isEmpty(tempPath)) {
                continue;
            }

            if (!tempPath.startsWith(prefix)) {
                continue;
            }

            if (isSpecialFile(tempPath)) {
                continue;
            }

            if (isPathInList(tempPath)){
                continue;
            }

            if (!checkMountPath(tempPath)) {
                continue;
            }

            Log.i(TAG, "tempPath " + tempPath);

            devicePaths[index] = tempPath;

            index++;
        }

        if (index == 0) {
            return null;
        }

        String[] outDeviceList = new String[index];
        for (int item = 0; item < index; item++) {
            outDeviceList[item] = devicePaths[item];
        }

        return outDeviceList;
    }
};
