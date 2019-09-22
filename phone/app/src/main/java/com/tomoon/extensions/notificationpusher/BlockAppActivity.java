package com.tomoon.extensions.notificationpusher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BlockAppActivity extends Activity {

    BlockAppApdater adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_app);
        final ProgressDialog pdd = new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
        pdd.setCancelable(false);
        pdd.setMessage("正在加载应用列表");
        pdd.show();
        new Thread(){
            @Override
            public void run() {
                adapter = new BlockAppApdater();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ListView)findViewById(R.id.listBlockedApp)).setAdapter(adapter);
                        pdd.dismiss();
                    }
                });
            }
        }.start();

    }

    class BlockAppApdater extends BaseAdapter{
        ArrayList<String> selections = new ArrayList<>();
        ArrayList<AppInfo> appList = new ArrayList<>();

        public BlockAppApdater(){
            PackageManager mPackageManager = getApplicationContext().getPackageManager();
            for (PackageInfo packageInfo :(ArrayList<PackageInfo>) mPackageManager.getInstalledPackages(0)) {
                boolean isSystem=false;
                try {
                    isSystem = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                }catch (Exception ex){}
                appList.add(new AppInfo(getApplicationName(packageInfo.packageName,mPackageManager),packageInfo.packageName,isSystem));
            }
            Collections.sort(appList);
            Set<String> apps =SpUtils.getConnSp(BlockAppActivity.this).getStringSet("bapps",null);
            if(null!=apps){
                selections.addAll(apps);
            }
        }
        public String getApplicationName(String packageName, PackageManager packageManager) {
            String applicationName = packageName;
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {

            }
            return applicationName;
        }
        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public AppInfo getItem(int i) {
            return appList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = getLayoutInflater().inflate(R.layout.adapter_checkedapp,null,false);
            AppInfo apf = getItem(i);
            ((TextView)v.findViewById(R.id.txtAppName)).setText(apf.name);
            ((TextView)v.findViewById(R.id.txtPkgName)).setText(apf.pkgname);
            ((CompoundButton)v.findViewById(R.id.chkSelection)).setChecked(selections.contains(apf.pkgname));
            ((CompoundButton)v.findViewById(R.id.chkSelection)).setOnCheckedChangeListener(new Checker(apf));
            return v;
        }

        class Checker implements CompoundButton.OnCheckedChangeListener{
            AppInfo target;

            public Checker(AppInfo target) {
                this.target = target;
            }

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    if(selections.contains(target.pkgname)){
                        selections.remove(target.pkgname);
                    }
                }else {
                    if(!selections.contains(target.pkgname)){
                        selections.add(target.pkgname);
                    }
                }
            }
        }

    }
    class AppInfo implements Comparable<AppInfo>{
        String name,pkgname;
        boolean isSystemApp;

        public AppInfo(String name, String pkgname, boolean isSystemApp) {
            this.name = name;
            this.pkgname = pkgname;
            this.isSystemApp = isSystemApp;
        }

        @Override
        public int compareTo(@NonNull AppInfo appInfo) {
            if(appInfo.isSystemApp == this.isSystemApp){return this.name.compareTo(appInfo.name);}
            if(!appInfo.isSystemApp && this.isSystemApp){return 1;}
            if(appInfo.isSystemApp && !this.isSystemApp){return -1;}
            return 0;
        }
    }

    @Override
    public void onBackPressed() {
        //SpUtils.getConnSp(BlockAppActivity.this).getStringSet("bapps",null);

        Set<String> blockedApp  = new HashSet<>();

        for (String pkg :
                adapter.selections) {
            if (!blockedApp.contains(pkg)) {
                blockedApp.add(pkg);
            }
        }
        SpUtils.getConnSp(BlockAppActivity.this).edit().remove("bapps").putStringSet("bapps",blockedApp).commit();
        NotificationService.loadConfiguration(this);
        super.onBackPressed();
    }
}
