package com.idotools.http.utils;

import java.io.File;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

public class PackageUtils {
    private static final String TAG = "PackageUtils";

    public static boolean isAppInstalled(Context context, String pkgName) {
        // PackageManager pm = context.getPackageManager();
        // boolean installed = false;
        // try {
        // pm.getPackageInfo(pkgName, 0);
        // installed = true;
        // } catch (NameNotFoundException e) {
        // // ignore the exception
        // }
        // return installed;

        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            // e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    public static String getVersionName(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(pkgName, 0);
            return pi.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getVersionCode(Context cxt, String pkgName, int defValue) {
        try {
            PackageManager pm = cxt.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(pkgName, 0);
            return pi.versionCode;
        } catch (Exception e) {
            // ignore
        }
        return defValue;
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = null;
        try {
            serviceInfos = manager.getRunningServices(Integer.MAX_VALUE);
            if (null == serviceInfos || 0 == serviceInfos.size())
                return false;
            for (ActivityManager.RunningServiceInfo service : serviceInfos) {
                if (serviceName.equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;
    }

    public static boolean isInBlackList(String pkgName) {
        // TODO: We need to maintain a white list in DB
        if (pkgName.equalsIgnoreCase("com.android.camera")) {
            return true;
        }
        return false;
    }

    public static void startupApp(Context context, String pkgName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasLaunchIntent(Context cxt, String pkg) {
        PackageManager pm = cxt.getPackageManager();
        if (pm.getLaunchIntentForPackage(pkg) != null) {
            return true;
        }
        return false;
    }

    public static PackageInfo getArchievePkgInfo(Context context, String apkFilepath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageArchiveInfo(apkFilepath, 0);
        } catch (Exception e) {
            // should be something wrong with parse
            e.printStackTrace();
        }
        if (pkgInfo == null) {
            return null;
        }
        return pkgInfo;
    }

    /*
     * 获取程序 图标
     */
    public static Bitmap getAppIcon(Context context, String packname) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packname, 0);
            Drawable d = info.loadIcon(pm);
            return ((BitmapDrawable) d).getBitmap();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void installApp(Context context, String path) {
        try {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Intent getInstallAppIntent(Context context, String path) {
        try {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        } catch (Exception e) {
            return null;
        }
    }
}
