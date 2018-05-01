package com.zhou.appmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.zhou.appmanager.MyAdapter.MyAdapter;
import com.zhou.appmanager.model.AppInfo;
import com.zhou.appmanager.util.AppUtil;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ImageView gif_loading;

    List<AppInfo> userAppInfos;
    List<AppInfo> systemAppInfos;
    private MenuItem firstMenuItem;
    private int sortByName=1;
    private int sortByPermissions=1;
    private int sortBySize=1;

    private MyAdapter myAdapter;
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

        //动态申请权限  WRITE_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
            }
        }

        //动态申请权限  READ_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);
            }
        }

        new Thread(runnable).start();
    }

    //在子线程中初始化数据并加载listview，因为用户手机中的应用越多加载时间就越长，所以不能在主线程中加载listview
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            //获取已安装的app信息
            systemAppInfos = AppUtil.getAppInfo(AppUtil.SYSTEM_APP,MainActivity.this);
            userAppInfos= AppUtil.getAppInfo(AppUtil.USER_APP,MainActivity.this);

            //根据名称排序
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    return o1.getAppName().compareTo(o2.getAppName());
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    return o1.getAppName().compareTo(o2.getAppName());
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView = findViewById(R.id.lv_baseAdapter);

                    myAdapter = new MyAdapter(userAppInfos, MainActivity.this);

                    gif_loading.setVisibility(View.GONE);
                    listView.setAdapter(myAdapter);

                    //给listView设置item点击监听
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        /**
                         *
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

                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            //删除当前行
                            //删除当前行的数据
                            userAppInfos.remove(position);
                            //更新列表
                            //listView.setAdapter(myAdapter);//显示列表，不会使用缓存的item的视图对象
                            myAdapter.notifyDataSetChanged();//通知显示列表，使用所有缓存的item的视图对象
                            return true;
                        }
                    });
                }
            });
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
        return super.onCreateOptionsMenu(menu);
    }

    //OptionsItem点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showApp:
                if (item.getTitle().toString().equals("显示系统应用")) {
                    myAdapter = new MyAdapter(systemAppInfos, MainActivity.this);
                    listView.setAdapter(myAdapter);
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
                    myAdapter = new MyAdapter(userAppInfos, MainActivity.this);
                    listView.setAdapter(myAdapter);
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
                    myAdapter = new MyAdapter(userAppInfos,MainActivity.this);
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
                    myAdapter = new MyAdapter(systemAppInfos,MainActivity.this);
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

                listView.setAdapter(myAdapter);
                break;
            case R.id.sortByPermissions:
                sortByPermissions();
                sortByPermissions++;
                //如果第一个菜单项是‘显示系统应用’，说明当前显示的是用户应用
                if (firstMenuItem.getTitle().toString().equals("显示系统应用")){
                    myAdapter = new MyAdapter(userAppInfos,MainActivity.this);
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
                    myAdapter = new MyAdapter(systemAppInfos,MainActivity.this);
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
                listView.setAdapter(myAdapter);
                break;
            case R.id.sortBySize:
                sortBySize();
                sortBySize++;
                //如果第一个菜单项是‘显示系统应用’，说明当前显示的是用户应用
                if (firstMenuItem.getTitle().toString().equals("显示系统应用")){
                    myAdapter = new MyAdapter(userAppInfos,MainActivity.this);
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
                    myAdapter = new MyAdapter(systemAppInfos,MainActivity.this);
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
                listView.setAdapter(myAdapter);
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
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    return o1.getAppName().compareTo(o2.getAppName());
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    return o1.getAppName().compareTo(o2.getAppName());
                }
            });
        } else {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    return o2.getAppName().compareTo(o1.getAppName());
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    return o2.getAppName().compareTo(o1.getAppName());
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
}
