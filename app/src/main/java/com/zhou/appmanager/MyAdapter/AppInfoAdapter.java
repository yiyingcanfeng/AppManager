package com.zhou.appmanager.MyAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.zhou.appmanager.R;
import com.zhou.appmanager.model.AppInfo;

import java.io.File;
import java.util.List;

//自定义adapter
public class AppInfoAdapter extends BaseAdapter {
    private List<AppInfo> appInfos;
    private Context context;
    public AppInfoAdapter(List<AppInfo> list, Context context) {
        this.appInfos = list;
        this.context = context;
    }

    //计算需要适配的item总数
    @Override
    public int getCount() {
        return appInfos.size();
    }

    //获取每一个item对象
    @Override
    public Object getItem(int position) {
        return appInfos.get(position);
    }

    //获取每一个item的ID值
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //View view = View.inflate(BaseAdapterActivity.this, R.layout.item_base_adapter, null);
        Holder holder;
        //如果convertView是null，加载item的布局文件
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_base_adapter, null);
            holder = new Holder();
            //实例化对象
            holder.imageView = convertView.findViewById(R.id.appIcon);
            holder.appName = convertView.findViewById(R.id.appName);
            holder.packageName = convertView.findViewById(R.id.packageName);
            holder.appPermission = convertView.findViewById(R.id.appPermission);
            holder.appSize = convertView.findViewById(R.id.appSize);

            //打标签
            convertView.setTag(holder);
        } else {
            //进行复用
            holder= (Holder) convertView.getTag();
        }
        //赋值
        AppInfo appInfo = appInfos.get(position);
        holder.imageView.setImageDrawable(appInfo.getAppIcon());
        holder.appName.setText(appInfo.getAppName());
        holder.packageName.setText(appInfo.getPackageName());
        holder.appPermission.setText("权限:"+ appInfo.getPermissionInfos().length);
        String appSize;
        File file = new File(appInfo.getApplicationInfo().sourceDir);
        if (file.length() / 1000 > 1024) {
            appSize = String.format("%.2f", file.length() / 1048576.0)+"MB";
        } else {
            appSize=file.length()/1024+"KB";
        }
        holder.appSize.setText("大小:"+appSize);

        return convertView;
    }
    class Holder{
        ImageView imageView;
        TextView appName;
        TextView packageName;
        TextView appPermission;
        TextView appSize;
    }
}
