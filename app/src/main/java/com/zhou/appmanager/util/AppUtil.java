package com.zhou.appmanager.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

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

    public static void exceptionToast(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
