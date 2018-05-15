package com.zhou.appmanager.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.zhou.appmanager.MainActivity;
import com.zhou.appmanager.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class AppUtil {
    public final static int USER_APP = 1;
    public final static int SYSTEM_APP = 2;

    //获取已安装的app信息
    public static List<AppInfo> getAppInfo(int tag, Context context) {
        PackageManager pm = context.getPackageManager();
        //获取所有的app信息
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        List<AppInfo> userApp = new ArrayList<>();
        List<AppInfo> systemApp = new ArrayList<>();

        for (PackageInfo packageInfo : packages) {
            // 判断系统/非系统应用
            AppInfo appInfo = new AppInfo();
            appInfo.setAppName(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
            appInfo.setPackageName(packageInfo.packageName);
            appInfo.setAppIcon( packageInfo.applicationInfo.loadIcon(context.getPackageManager()));
            appInfo.setApplicationInfo(packageInfo.applicationInfo);
            try {
                String[] permissions=pm.getPackageInfo(appInfo.getPackageName(),PackageManager.GET_PERMISSIONS).requestedPermissions;
                if (permissions == null) {
                    permissions= new String[]{};
                }
                appInfo.setPermissionInfos(permissions);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {// 非系统应用
                userApp.add(appInfo);
            } else {
                systemApp.add(appInfo);
            }
        }

        if (tag == SYSTEM_APP) {
            return systemApp;
        } else if (tag == USER_APP) {
            return userApp;
        } else {
            return null;
        }
    }

    //抛出异常时打吐司
    public static void exceptionToast(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    //申请权限
    public static void requestPermissions(Activity activity) {
        //动态申请权限  WRITE_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
            }
        }

        //动态申请权限  READ_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);
            }
        }
    }
}
