package com.zhou.appmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhou.appmanager.MyAdapter.AppInfoAdapter;
import com.zhou.appmanager.model.AppInfo;
import com.zhou.appmanager.util.AppUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String alipay_person_qr="HTTPS://QR.ALIPAY.COM/FKX03040WLPHIWRHMSGXD6";

    private ListView listView;
    private ImageView gif_loading;

    List<AppInfo> userAppInfos;
    List<AppInfo> userAppInfosOld;
    List<AppInfo> systemAppInfos;
    List<AppInfo> systemAppInfosOld;
    private MenuItem firstMenuItem;
    private int sortByName=1;
    private int sortByPermissions=1;
    private int sortBySize=1;
    private SearchView mSearchView;
    private AppInfoAdapter appInfoAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gif_loading = findViewById(R.id.gif_loading);
        //显示一个"正在加载"的gif动图，提示用户加载的时间可能较长，然后开启子线程
        Glide.with(MainActivity.this)
                .load(R.mipmap.loading)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(gif_loading);
        //申请权限
        AppUtil.requestPermissions(this);
        new Thread(runnable).start();
    }

    //在子线程中初始化数据并加载listview，因为用户手机中的应用越多加载时间就越长，所以不能在主线程中加载listview
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            long datainitstart = SystemClock.currentThreadTimeMillis();
            //获取已安装的app信息
            userAppInfos= AppUtil.getAppInfo(AppUtil.USER_APP,MainActivity.this);
            systemAppInfos = AppUtil.getAppInfo(AppUtil.SYSTEM_APP,MainActivity.this);
            userAppInfosOld = userAppInfos;
            systemAppInfosOld = systemAppInfos;
            //根据名称排序
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o1.getAppName(), o2.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    //return o1.getAppName().compareTo(o2.getAppName());
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o1.getAppName(), o2.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    //return o1.getAppName().compareTo(o2.getAppName());
                }
            });
            long datainitend = SystemClock.currentThreadTimeMillis();
            Log.i("datainittime", datainitend-datainitstart+"");
            long uiinitstart = SystemClock.currentThreadTimeMillis();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView = findViewById(R.id.lv_baseAdapter);

                    appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);

                    gif_loading.setVisibility(View.GONE);
                    listView.setAdapter(appInfoAdapter);

                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        /**
                         * @param parent listview
                         * @param view 当前行的item的view对象
                         * @param position 当前行的下标
                         * @param id
                         */
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = userAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo",appInfo);

                            //Android8.0之后，系统应用默认图标实现变成AdaptiveIconDrawable,不能转型成BitmapDrawable
                            Drawable drawable = appInfo.getAppIcon();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bm = bd.getBitmap();
                                intent.putExtra("appIcon", bm);
                                startActivity(intent);
                            } else {
                                startActivity(intent);
                            }
                        }
                    });
                }
            });
            long uiinitend = SystemClock.currentThreadTimeMillis();
            Log.i("uiinittime", uiinitend-uiinitstart+"");
        }
    };


    //创建并初始化OptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //得到菜单加载器对象
        MenuInflater menuInflater=getMenuInflater();
        //加载菜单文件
        menuInflater.inflate(R.menu.option_menu,menu);

        firstMenuItem=menu.findItem(R.id.showApp);

        //标题栏搜索框功能实现
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        mSearchView.setQueryHint("请输入要搜索的应用名称");//设置输入框提示语
        mSearchView.setIconified(true);//设置searchView是否处于展开状态 false:展开
        //mSearchView.setIconifiedByDefault(false);
        //mSearchView.onActionViewExpanded();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //提交按钮的点击事件
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            //当输入框内容改变的时候回调方法
            @Override
            public boolean onQueryTextChange(String newText) {
                //如果第一个菜单项是‘显示系统应用’，说明当前显示的是用户应用
                try {
                    if (firstMenuItem.getTitle().toString().equals("显示系统应用")) {
                        if (!newText.equals("")) {
                            userAppInfos = userAppInfosOld;
                            List<AppInfo> userAppInfosNew = new ArrayList<>();

                            long searchstart = System.currentTimeMillis();
                            AppInfo appInfo;
                            for (int i = 0; i < userAppInfos.size(); i++) {
                                appInfo = userAppInfos.get(i);
                                //支持通过拼音进行模糊搜索带中文名称的app PinyinTool.getPinyinString(appInfo.getAppName()).contains(newText)||
                                if (appInfo.getAppName().toLowerCase().contains(newText)||appInfo.getAppNamePinyin().toLowerCase().contains(newText)) {
                                    userAppInfosNew.add(appInfo);
                                }

                            }
                            long searchend = System.currentTimeMillis();
                            Log.e("searchtimeused", searchend-searchstart+"");
                            userAppInfos = userAppInfosNew;
                            //appInfoAdapter.notifyDataSetInvalidated();
                            appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);
                            listView.setAdapter(appInfoAdapter);
                        } else {//如果输入框内容为空，则显示全部app
                            userAppInfos = userAppInfosOld;
                            //appInfoAdapter.notifyDataSetInvalidated();
                            appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);
                            listView.setAdapter(appInfoAdapter);
                        }
                    } else {
                        if (!newText.equals("")) {
                            systemAppInfos = systemAppInfosOld;
                            List<AppInfo> systemAppInfosNew = new ArrayList<>();
                            AppInfo appInfo;
                            for (int i = 0; i < systemAppInfos.size(); i++) {
                                appInfo = systemAppInfos.get(i);
                                //支持通过拼音进行模糊搜索带中文名字的app PinyinTool.getPinyinString(appInfo.getAppName()).contains(newText)||
                                if (appInfo.getAppName().toLowerCase().contains(newText)||appInfo.getAppNamePinyin().toLowerCase().contains(newText)) {
                                    systemAppInfosNew.add(appInfo);
                                }
                            }
                            systemAppInfos = systemAppInfosNew;
                            appInfoAdapter = new AppInfoAdapter(systemAppInfos, MainActivity.this);
                            listView.setAdapter(appInfoAdapter);
                        } else {//如果输入框内容为空，则显示全部app
                            systemAppInfos = systemAppInfosOld;
                            appInfoAdapter = new AppInfoAdapter(systemAppInfos, MainActivity.this);
                            listView.setAdapter(appInfoAdapter);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //OptionsItem点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showApp:
                //如果第一个菜单项是‘显示系统应用’，说明当前显示的是用户应用
                if (item.getTitle().toString().equals("显示系统应用")) {
                    systemAppInfos = systemAppInfosOld;
                    appInfoAdapter = new AppInfoAdapter(systemAppInfos, MainActivity.this);
                    listView.setAdapter(appInfoAdapter);
                    item.setTitle("显示用户应用");
                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = systemAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo",appInfo);

                            //Android8.0之后，系统默认图标实现变成AdaptiveIconDrawable,不能转型成BitmapDrawable
                            Drawable drawable = appInfo.getAppIcon();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bm = bd.getBitmap();
                                intent.putExtra("appIcon", bm);
                                startActivity(intent);
                            } else {
                                startActivity(intent);
                            }

                        }
                    });
                } else {
                    userAppInfos = userAppInfosOld;
                    appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);
                    listView.setAdapter(appInfoAdapter);
                    item.setTitle("显示系统应用");
                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = userAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo",appInfo);

                            //Android8.0之后，系统默认图标实现变成AdaptiveIconDrawable,不能转型成BitmapDrawable
                            Drawable drawable = appInfo.getAppIcon();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bm = bd.getBitmap();
                                intent.putExtra("appIcon", bm);
                                startActivity(intent);
                            } else {
                                startActivity(intent);
                            }
                        }
                    });
                }

                break;
            case R.id.sortByName:
                sortByName();
                sortByName++;
                //如果第一个菜单项是‘显示系统应用’，说明当前显示的是用户应用
                if (firstMenuItem.getTitle().toString().equals("显示系统应用")){
                    userAppInfos = userAppInfosOld;
                    appInfoAdapter = new AppInfoAdapter(userAppInfos,MainActivity.this);
                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = userAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo",appInfo);

                            //Android8.0之后，系统默认图标实现变成AdaptiveIconDrawable,不能转型成BitmapDrawable
                            Drawable drawable = appInfo.getAppIcon();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bm = bd.getBitmap();
                                intent.putExtra("appIcon", bm);
                                startActivity(intent);
                            } else {
                                startActivity(intent);
                            }
                        }
                    });
                } else if (firstMenuItem.getTitle().toString().equals("显示用户应用")) {
                    systemAppInfos = systemAppInfosOld;
                    appInfoAdapter = new AppInfoAdapter(systemAppInfos,MainActivity.this);
                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = systemAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo",appInfo);

                            //Android8.0之后，系统默认图标实现变成AdaptiveIconDrawable,不能转型成BitmapDrawable
                            Drawable drawable = appInfo.getAppIcon();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bm = bd.getBitmap();
                                intent.putExtra("appIcon", bm);
                                startActivity(intent);
                            } else {
                                startActivity(intent);
                            }
                        }
                    });
                }

                listView.setAdapter(appInfoAdapter);
                break;
            case R.id.sortByPermissions:
                sortByPermissions();
                sortByPermissions++;
                //如果第一个菜单项是‘显示系统应用’，说明当前显示的是用户应用
                if (firstMenuItem.getTitle().toString().equals("显示系统应用")){
                    userAppInfos = userAppInfosOld;
                    appInfoAdapter = new AppInfoAdapter(userAppInfos,MainActivity.this);
                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = userAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo",appInfo);

                            //Android8.0之后，系统默认图标实现变成AdaptiveIconDrawable,不能转型成BitmapDrawable
                            Drawable drawable = appInfo.getAppIcon();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bm = bd.getBitmap();
                                intent.putExtra("appIcon", bm);
                                startActivity(intent);
                            } else {
                                startActivity(intent);
                            }
                        }
                    });
                } else if (firstMenuItem.getTitle().toString().equals("显示用户应用")) {
                    systemAppInfos = systemAppInfosOld;
                    appInfoAdapter = new AppInfoAdapter(systemAppInfos,MainActivity.this);
                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = systemAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo",appInfo);

                            //Android8.0之后，系统默认图标实现变成AdaptiveIconDrawable,不能转型成BitmapDrawable
                            Drawable drawable = appInfo.getAppIcon();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bm = bd.getBitmap();
                                intent.putExtra("appIcon", bm);
                                startActivity(intent);
                            } else {
                                startActivity(intent);
                            }
                        }
                    });
                }
                listView.setAdapter(appInfoAdapter);
                break;
            case R.id.sortBySize:
                sortBySize();
                sortBySize++;
                //如果第一个菜单项是‘显示系统应用’，说明当前显示的是用户应用
                if (firstMenuItem.getTitle().toString().equals("显示系统应用")){
                    appInfoAdapter = new AppInfoAdapter(userAppInfos,MainActivity.this);
                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = userAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo",appInfo);

                            //Android8.0之后，系统默认图标实现变成AdaptiveIconDrawable,不能转型成BitmapDrawable
                            Drawable drawable = appInfo.getAppIcon();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bm = bd.getBitmap();
                                intent.putExtra("appIcon", bm);
                                startActivity(intent);
                            } else {
                                startActivity(intent);
                            }
                        }
                    });
                } else if (firstMenuItem.getTitle().toString().equals("显示用户应用")) {
                    appInfoAdapter = new AppInfoAdapter(systemAppInfos,MainActivity.this);
                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = systemAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo",appInfo);

                            //Android8.0之后，系统默认图标实现变成AdaptiveIconDrawable,不能转型成BitmapDrawable
                            Drawable drawable = appInfo.getAppIcon();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bm = bd.getBitmap();
                                intent.putExtra("appIcon", bm);
                                startActivity(intent);
                            } else {
                                startActivity(intent);
                            }
                        }
                    });
                }
                listView.setAdapter(appInfoAdapter);
                break;
            case R.id.donate:
                String qrcode = "";
                try {
                    qrcode = URLEncoder.encode(alipay_person_qr, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (checkAliPayInstalled(this)) {
                    final String alipayqr = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + qrcode;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alipayqr + "%3F_s%3Dweb-other&_t=" + System.currentTimeMillis()));
                    MainActivity.this.startActivity(intent);
                }else {
                    Toast.makeText(this, "你还未安装支付宝", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alipay_person_qr.toLowerCase()));
                    MainActivity.this.startActivity(intent);
                }
                break;
            case R.id.exit:
                finish();
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //根据名称排序
    private void sortByName(){
        if (sortByName % 2 == 0) {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o1.getAppName(), o2.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    //return o1.getAppName().compareTo(o2.getAppName());
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o1.getAppName(), o2.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    //return o1.getAppName().compareTo(o2.getAppName());
                }
            });
        } else {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o2.getAppName(), o1.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o2.getAppName(), o1.getAppName());
                    //return o2.getAppName().compareTo(o1.getAppName());
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o2.getAppName(), o1.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o2.getAppName(), o1.getAppName());
                    //return o2.getAppName().compareTo(o1.getAppName());
                }
            });
        }
    }
    //根据权限数量排序
    private void sortByPermissions() {
        if (sortByPermissions % 2 == 0) {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = o1.getPermissionInfos().length - o2.getPermissionInfos().length;
                    if (i > 0) {
                        return 1;
                    } else if (i == 0) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = o1.getPermissionInfos().length - o2.getPermissionInfos().length;
                    if (i > 0) {
                        return 1;
                    } else if (i == 0) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
        } else {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = o1.getPermissionInfos().length - o2.getPermissionInfos().length;
                    if (i > 0) {
                        return -1;
                    } else if (i == 0) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = o1.getPermissionInfos().length - o2.getPermissionInfos().length;
                    if (i > 0) {
                        return -1;
                    } else if (i == 0) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
        }
    }

    //根据apk大小排序
    private void sortBySize() {
        if (sortBySize % 2 == 0) {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int)new File(o1.getApplicationInfo().sourceDir).length() - (int)new File(o2.getApplicationInfo().sourceDir).length();
                    if (i > 0) {
                        return 1;
                    } else if (i == 0) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int)new File(o1.getApplicationInfo().sourceDir).length() - (int)new File(o2.getApplicationInfo().sourceDir).length();
                    if (i > 0) {
                        return 1;
                    } else if (i == 0) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
        } else {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int)new File(o1.getApplicationInfo().sourceDir).length() - (int)new File(o2.getApplicationInfo().sourceDir).length();
                    if (i > 0) {
                        return -1;
                    } else if (i == 0) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int)new File(o1.getApplicationInfo().sourceDir).length() - (int)new File(o2.getApplicationInfo().sourceDir).length();
                    if (i > 0) {
                        return -1;
                    } else if (i == 0) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
        }
    }

    //检测是否安装了支付宝
    public static boolean checkAliPayInstalled(Context context) {
        Uri uri = Uri.parse("alipays://platformapi/startApp");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ComponentName componentName = intent.resolveActivity(context.getPackageManager());
        return componentName != null;
    }

}
