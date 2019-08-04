package com.yx.srtool.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.yx.srtool.Adapter.Bean;
import com.yx.srtool.Adapter.GalaxyAdapter;
import com.yx.srtool.Adapter.ModifierAdapter;
import com.yx.srtool.Adapter.ShipAdapter;
import com.yx.srtool.R;
import com.yx.srtool.Utils.FileUtil;
import com.yx.srtool.Utils.HttpUtil;
import com.yx.srtool.Utils.Util;
import com.yx.srtool.i.AboutPoint;
import com.yx.srtool.i.GalaxyDesignPoint;
import com.yx.srtool.i.RocketDesignPoint;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TabLayout mTabLayout;
    ViewPager mViewPager;
    private static RecyclerView recyclerview_ship //载具
            ,recyclerview_modifier
            ,recyclerview_galaxy;//星系
    private static GridView gridview_auxiliary;//辅助
    private static Context TContext;
    private static List<Bean> data_ship,data_galaxy,data_modifier;
    private static ProgressDialog progressdialog;
    public static String ship_name//载具名称
            ,galaxy_name;//星系名称
    public static String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/Android/data/com.jundroo.simplerockets/files/";

    int statusBarHeight = -1;//状态栏高度.
    ConstraintLayout toucherLayout;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TContext = this;
        try{
            //初始化标题栏
            init_toolbar();
            //初始化权限
            init_permission();
            //初始化
            init();
        }catch (Exception e){
        }
    }
    /**
     * 从后台打开
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //刷新载具列表
        newship();
        //刷新星系列表
        newgalaxy();
    }

    /**
     * 初始化
     */
    private void init() {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File file =new File(path+"ships");
            //如果文件夹不存在则创建
            if  (!file .exists()  && !file .isDirectory())
            {
                file .mkdir();
            }
            file =new File(path+"galaxy");
            //如果文件夹不存在则创建
            if  (!file .exists()  && !file .isDirectory())
            {
                file .mkdir();
                //星系复制
                FileUtil.CopySdcardFile(MainActivity.path+"SmolarSystem.xml", MainActivity.path+"galaxy/SmolarSystem.xml");
            }
        }
        //查找视图
        mTabLayout = (TabLayout) findViewById(R.id.search_tablayout);
        mViewPager = (ViewPager) findViewById(R.id.search_pager);
        //初始化视图
        initView();
        //被浏览器调用
        Intent intent = getIntent();
        String action = intent.getAction();
        if(Intent.ACTION_VIEW.equals(action)){
            Uri uri = intent.getData();
            String ID = uri.getQueryParameter("id");
            if(ID!=null){
                //下载存档
                DownloadFile(ID);
            }
        }
        if(intent.getStringExtra("mod").indexOf("true")!=-1){
            Toast.makeText(TContext, "导入Mod", Toast.LENGTH_SHORT).show();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.jundroo.simplerockets", "com.jundroo.simplerockets.MainActivity");
            Uri uri = Uri.fromFile(new File(intent.getStringExtra("path")));
            intent.setData(uri);
            startActivityForResult(intent, 1);
        }
    }

    /**
     * 初始化权限
     */
    private void init_permission() {
        if(FileUtil.getFilesAllName(Environment.getExternalStorageDirectory().getAbsolutePath()) ==null){
            new AlertDialog.Builder(getContext())
                    .setCancelable(false)
                    .setMessage("本应用需要读写存储器权限来读取载具以及MOD，请在应用设置中手动勾选~")
                    .setTitle("错误")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    }).show();
        }
    }

    /**
     * 初始化标题栏
     */
    private void init_toolbar() {
        Toolbar toolbar= (Toolbar) findViewById(R.id.search_toolbar);//获取组件
        toolbar.setTitle("");//设置标题
        setSupportActionBar(toolbar);//替换原生toolbar
    }

    String[] Tab_Title = new String[3];
    /**
     * 初始化视图
     */
    private void initView() {
        Tab_Title[0] = "载具";//获取SR目录载具
        Tab_Title[1] = "星系";//星系目录
        Tab_Title[2] = "辅助";//辅助修改

        data_ship = new ArrayList<Bean>();
        data_galaxy = new ArrayList<Bean>();
        data_modifier = new ArrayList<Bean>();
        //定义一个视图集合（用来装左右滑动的页面视图）
        final List<View> viewList = new ArrayList<View>();
        //定义几个视图，每个视图都加载同一个布局文件
        View view0 = getLayoutInflater().inflate(R.layout.view_recycler,null);
        View view1 = getLayoutInflater().inflate(R.layout.view_recycler,null);
        View view2 = getLayoutInflater().inflate(R.layout.view_gridview,null);
        //将每个视图添加到视图集合中
        viewList.add(view0);
        viewList.add(view1);
        viewList.add(view2);
        //为ViewPager设置适配器
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public CharSequence getPageTitle(int position) {
                return Tab_Title[position % Tab_Title.length];
            }
            @Override
            public int getCount() {
                //这个方法是返回总共有几个滑动的页面（）
                return Tab_Title.length;
            }
            @Override
            public boolean isViewFromObject(View view, Object object) {
                //该方法判断是否由该对象生成界面。
                return view==object;
            }
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                //这个方法返回一个对象，该对象表明PagerAapter选择哪个对象放在当前的ViewPager中。这里我们返回当前的页面
                mViewPager.addView(viewList.get(position));
                return viewList.get(position);
            }
            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                //这个方法从viewPager中移动当前的view。（划过的时候）
                mViewPager.removeView(viewList.get(position));
            }
        });
        //将ViewPager关联到TabLayout上
        mTabLayout.setupWithViewPager(mViewPager);

        RecyclerView.LayoutManager manager0=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerview_ship= (RecyclerView) view0.findViewById(R.id.recyclerView);
        recyclerview_ship.setItemAnimator(new DefaultItemAnimator());
        recyclerview_ship.setLayoutManager(manager0);

        RecyclerView.LayoutManager manager1=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerview_galaxy= (RecyclerView) view1.findViewById(R.id.recyclerView);
        recyclerview_galaxy.setItemAnimator(new DefaultItemAnimator());
        recyclerview_galaxy.setLayoutManager(manager1);

        final CheckBox CheckBox_FPS = (CheckBox) view2.findViewById(R.id.CheckBox_FPS);
        String data = null;
        try {
            //读取配置文件
            data = FileUtil.read(MainActivity.path+"SimpleRockets-UserSettings.txt");
            String data2 = Util.substring(data,"fpsEnabled=\"","\"");
            if (data2!=null && data2.equals("1")){
                CheckBox_FPS.setChecked(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //监听CheckBox
        final String finalData = data;
        CheckBox_FPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //保存配置文件
                    FileUtil.write("SimpleRockets-UserSettings.txt", finalData.replace("fpsEnabled=\"0\"","fpsEnabled=\"1\""));
                    Toast.makeText(MainActivity.this, "成功开启FPS显示~", Toast.LENGTH_SHORT).show();
                }else {
                    FileUtil.write("SimpleRockets-UserSettings.txt", finalData.replace("fpsEnabled=\"1\"","fpsEnabled=\"0\""));
                    Toast.makeText(MainActivity.this, "成功关闭FPS显示~", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button Button_Modifiers = (Button) view2.findViewById(R.id.Button_Modifiers);
        //监听Button
        Button_Modifiers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "打开修改器", Toast.LENGTH_SHORT).show();

                if(Build.VERSION.SDK_INT>=23){
                    if(Settings.canDrawOverlays(MainActivity.this))
                    {//有悬浮窗权限开启服务绑定 绑定权限
                        createToucher();
                    }else{//没有悬浮窗权限m,去开启悬浮窗权限
                        try{
                            //没有权限
                            Toast.makeText(MainActivity.this, "当前无权限，请授权！", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 1234);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } else{//默认有悬浮窗权限  但是 华为, 小米,oppo等手机会有自己的一套Android6.0以下  会有自己的一套悬浮窗权限管理 也需要做适配
                    createToucher();
                }
            }
        });
        //加载载具
        newship();
        //视图滑动事件
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //切换ViewPager
                mViewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        //刷新载具
                        newship();
                        break;
                    case 1:
                        //刷新星系
                        newgalaxy();
                        break;
                    case 2:
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void createToucher() {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        /*
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//https://blog.csdn.net/qq_23374873/article/details/80718948
            //关于 8.0以上
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {

        }
        */
        //关于 8.0以下
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        //params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 200;

        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);

        //设置悬浮窗口长宽数据.
        params.width = 100;
        params.height = 100;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (ConstraintLayout) inflater.inflate(R.layout.toucherlayout, null);
        //添加toucherlayout
        windowManager.addView(toucherLayout, params);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        //浮动窗口按钮.
        ImageButton imageButton1 = (ImageButton) toucherLayout.findViewById(R.id.imageButton1);
        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出信息框
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View dialog_title = layoutInflater.inflate(R.layout.dialog_title, null);
                View dialog_view = layoutInflater.inflate(R.layout.view_recycler, null);

                TextView textView = (TextView) dialog_title.findViewById(R.id.Dialog_title);
                textView.setText("坐标修改");
                //View
                RecyclerView.LayoutManager manager1=new LinearLayoutManager(MainActivity.this,LinearLayoutManager.VERTICAL,false);
                recyclerview_modifier= (RecyclerView) dialog_view.findViewById(R.id.recyclerView);
                recyclerview_modifier.setItemAnimator(new DefaultItemAnimator());
                recyclerview_modifier.setLayoutManager(manager1);

                //载具目录适配器
                ModifierAdapter adapter=new ModifierAdapter(getContext(),data_modifier);
                //为recyclerview设置适配器
                recyclerview_modifier.setAdapter(adapter);

                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext())
                        .setView(dialog_view)
                        .setCustomTitle(dialog_title);
                final AlertDialog dialog = builder.create();

                dialog.getWindow().setType(
                        (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        dialog.show();
                    }
                });

                newsandbox();
            }
        });
        imageButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                event_operation=false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://0按下
                        break;
                    case MotionEvent.ACTION_UP://1抬起
                        //执行长按
                        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        DisplayMetrics dm = new DisplayMetrics();
                        wm.getDefaultDisplay().getMetrics(dm);
                        width = dm.widthPixels;         // 屏幕宽度（像素）
                        height = dm.heightPixels;       // 屏幕高度（像素）
                        event_operation=true;
                        handler_time.postDelayed(runnable,5);
                        break;
                    case MotionEvent.ACTION_MOVE://2移动
                        //-20取图片中心点
                        params.x = (int) event.getRawX() - 50;
                        params.y = (int) event.getRawY() - 50 - statusBarHeight;
                        windowManager.updateViewLayout(toucherLayout, params);
                        break;
                }
                return false;
            }
        });
        newsandbox();
    }
    int width;
    int height;
    Handler handler_time = new Handler();                //时间线程
    boolean event_operation;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(event_operation){
                if(params.x>width/2+20){
                    if(params.x<width){
                        params.x = (int) params.x + 20;
                        windowManager.updateViewLayout(toucherLayout, params);
                        handler_time.postDelayed(runnable,5);
                    }else {
                        Toast.makeText(MainActivity.this, "滑动", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if(params.x>0){
                        params.x = (int) params.x - 20;
                        windowManager.updateViewLayout(toucherLayout, params);
                        handler_time.postDelayed(runnable,5);
                    }else {
                        Toast.makeText(MainActivity.this, "滑动", Toast.LENGTH_SHORT).show();
                    }
                }
                //开始透明
            }

        }
    };
    /**
     * 刷新载具目录
     */
    public static void newsandbox() {
        //清除载具目录数据
        data_modifier.clear();
        try {
            String data = FileUtil.read(MainActivity.path+"Sandbox.xml");
            //分割字串符
            String[] ShipNode = data.split("</ShipNode>");
            Log.e("Sandbox",data);
            for (int i=0;ShipNode.length-1>i;i++){
                String data2 = Util.substring(ShipNode[i]+"</ShipNode>","<ShipNode","</ShipNode>");
                String x = Util.substring(data2,"x=\"","\"");
                String y = Util.substring(data2,"y=\"","\"");
                String vx = Util.substring(data2,"vx=\"","\"");
                String vy = Util.substring(data2,"vy=\"","\"");
                String name = Util.substring(data2,"name=\"","\"");

                Log.e("----","-----");
                Log.e("x",x);
                Log.e("y",y);
                Log.e("vx",vx);
                Log.e("vy",vy);

                Bean bean = new Bean(name,"x="+x+"|y="+y+"|vx="+vy+"|vy="+vy);
                data_modifier.add(bean);
                //载具目录适配器
                ShipAdapter adapter=new ShipAdapter(getContext(),data_modifier);

                //为recyclerview设置适配器
                recyclerview_modifier.setAdapter(adapter);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 刷新载具目录
     */
    public static void newship() {
        //清除载具目录数据
        data_ship.clear();
        //获取当前目录下所有文件名称
        List<File> FilesAllName = FileUtil.getFilesAllName(path+"ships");
        //列出目录
        for(int i=0;FilesAllName!=null&&i<FilesAllName.size();i++)
        {
            try {
                //读取载具文件内的数据
                String data = FileUtil.read2(FilesAllName.get(i));
                //当数据不为空
                if(data!=null){
                    String ID = "未上传";
                    if(data.indexOf("<!--ID")!=-1){
                        //取字串符中间字串符
                         ID = Util.substring(data,"<!--ID","-->");
                         if(ID==null || ID == "null" || ID.equals("null") || ID.equals("") || ID == ""){
                             ID = "未上传";
                         }else {
                             ID = "云端ID-->"+ID;
                         }
                    }
                    //获取载具文件名称
                    String name = FilesAllName.get(i).getName();
                    if(name!=null && !name.equals("")){
                        Bean bean = new Bean(name,ID);
                        data_ship.add(bean);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //载具目录适配器
        ShipAdapter adapter=new ShipAdapter(getContext(),data_ship);

        //为recyclerview设置适配器
        recyclerview_ship.setAdapter(adapter);
        //适配器内点击事件
        adapter.setOnitemClickLintener(new ShipAdapter.onHistoryClick() {
            @Override
            public void onClick(final String title, final String position) {
                final List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
                //设置dialog内的布局
                String name[]={"简单火箭","视图编辑","上传云端","重新命名"};
                for (int i = 0; i <name.length; i++) {
                    Map<String, Object> map=new HashMap<String, Object>();
                    map.put("text",name[i]);
                    dataList.add(map);
                }
                View dialog = View.inflate(getContext(),R.layout.dialog_view_ship,null);
                GridView dialog_view_ship_gridview = (GridView) dialog.findViewById(R.id.dialog_view_ship_gridview);
                String[] from={"text"};
                int[] to={R.id.text};
                SimpleAdapter adapter=new SimpleAdapter(getContext(), dataList, R.layout.dialog_view_ship_item, from, to);
                dialog_view_ship_gridview.setAdapter(adapter);
                //AlertDialog
                final AlertDialog alertdialog = new AlertDialog.Builder(getContext())
                        .setView(dialog).show();
                //Dialog布局内点击事件
                dialog_view_ship_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                            long arg3) {
                        switch (dataList.get(arg2).get("text").toString()){
                            case "简单火箭":
                                //打开简单火箭
                                PackageManager packageManager = getContext().getPackageManager();
                                Intent intent=new Intent();
                                intent =packageManager.getLaunchIntentForPackage("com.jundroo.simplerockets");
                                if(intent==null){
                                    Snackbar.make(recyclerview_ship, "未安装", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }else{
                                    getContext().startActivity(intent);
                                }
                                break;
                            case "视图编辑":
                                //打开视图编辑
                                Intent intent_activity = new Intent(getContext(), RocketDesignPoint.class);
                                ship_name = title;
                                getContext().startActivity(intent_activity);
                                break;
                            case "上传云端":
                                //上传云端
                                if(Util.equals(position,"未上传")){
                                    //显示下载加载log
                                    progressdialog = ProgressDialog.show(getContext(), "提示", "正在上传...", false);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                //获取载具数据
                                                File file = new File(path+"ships/"+title);
                                                String data = FileUtil.read2(file);
                                                //向官方服务器发送载具数据，返回id
                                                final String id = HttpUtil.RequestHTTP("http://jundroo.com/service/SimpleRockets/UploadRocket","POST","RocketVersion=1&RocketXml="+data,null,null);
                                                if(id!=null && id!="" && !id.equals("null") && !id.equals("") && id.length()>1){
                                                    try {
                                                        //将id重写进载具文件
                                                        file =new File(path+"ships/"+title);
                                                        String data3 = FileUtil.read2(file);
                                                        //判断id是否存在
                                                        if(Util.substring(data3,"<!--ID","-->")!=null){
                                                            //覆盖原id
                                                            FileUtil.write("ships/"+title,data3.replace(Util.substring(data3,"<!--ID","-->"),"<!--ID"+id+"-->"));
                                                        }else {
                                                            //写入id
                                                            FileUtil.write("ships/"+title,"<!--ID"+id+"-->\n"+data3);
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                Message mes = new Message();
                                                mes.obj = id+"";
                                                //0:上传 1：下载
                                                mes.what = 0;
                                                handler_ship.sendMessage(mes);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                }else {
                                    Snackbar.make(recyclerview_ship, "云端已存在", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                                break;
                            case "重新命名":
                                //new一个EditText
                                final EditText ditetext_name = new EditText(getContext());
                                View dialog = View.inflate(getContext(), R.layout.dialog_view,null);
                                LinearLayout dialog_layout = (LinearLayout) dialog.findViewById(R.id.dialog_layout);
                                //将EditText封装进布局中
                                dialog_layout.addView(ditetext_name);
                                //获取载具File
                                File file =new File(path+"ships/"+title);
                                try {
                                    //读取载具数据
                                    String data = FileUtil.read2(file);
                                    //URLDecoder：解析URL数据
                                    ditetext_name.setText(URLDecoder.decode(Util.substring(data,"name=\"","\""), "UTF-8"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //AlertDialog
                                new AlertDialog.Builder(getContext())
                                        .setView(dialog)
                                        .setTitle("命名")
                                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog1, int which) {
                                                if(!ditetext_name.getText().toString().equals("") && ditetext_name.getText().toString()!=null){
                                                    //旧的名称
                                                    File file =new File(path+"ships/"+title);
                                                    //新的名称
                                                    File newFile = new File(path+"ships/"+ditetext_name.getText().toString());
                                                    //重命名
                                                    file.renameTo(newFile);
                                                    //重命名完成
                                                    newship();//刷新
                                                    try {
                                                        //重写读取载具数据
                                                        file =new File(path+"ships/"+ditetext_name.getText());
                                                        String data = FileUtil.read2(file);
                                                        //重写写出文件
                                                        FileUtil.write("ships/"+ditetext_name.getText().toString(),data.replace(
                                                                "name=\""+ Util.substring(data,"name=\"","\"") +"\""//获取数据内地名称
                                                                , "name=\""+URLEncoder.encode(ditetext_name.getText().toString())+"\""));//获取新名称，并将其转换为URL格式(为了防止输入中文)
                                                        //通知
                                                        Snackbar.make(recyclerview_ship, "重命名成功", Snackbar.LENGTH_LONG)
                                                                .setAction("Action", null).show();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }else {
                                                    Snackbar.make(recyclerview_ship, "请勿输入空字符", Snackbar.LENGTH_LONG)
                                                            .setAction("Action", null).show();
                                                }
                                            }
                                        }).show();
                                break;
                        }
                        //关闭AlertDialog
                        alertdialog.dismiss();
                    }
                });
            }
        });
    }
    /**
     * 刷新星系目录
     */
    public static void newgalaxy() {
        //清除星系目录数据
        data_galaxy.clear();
        //获取当前目录下所有文件名称
        List<File> FilesAllName = FileUtil.getFilesAllName(path+"galaxy");
        //列出目录
        for(int i=0;FilesAllName!=null&&i<FilesAllName.size();i++)
        {
            try {
                //读取星系文件内的数据
                String data = FileUtil.read2(FilesAllName.get(i));
                //当数据不为空
                if(data!=null){
                    String ID = "未上传";
                    //取字串符中间字串符
                    if(data.indexOf("<!--ID")!=-1){
                        ID = "云端ID-->"+ Util.substring(data,"<!--ID","-->");
                    }
                    String CanonicalPath = FilesAllName.get(i).getCanonicalPath().toString().replace(path+"galaxy/","");
                    if(CanonicalPath != null && !CanonicalPath.equals("")){
                        Bean bean = new Bean(CanonicalPath,ID);
                        data_galaxy.add(bean);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GalaxyAdapter adapter=new GalaxyAdapter(getContext(),data_galaxy);
        recyclerview_galaxy.setAdapter(adapter);
        adapter.setOnitemClickLintener(new GalaxyAdapter.onHistoryClick() {
            @Override
            public void onClick(final String title, String position) {
                final List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
                String name[]={"简单火箭","视图编辑","上传云端","重新命名"};
                for (int i = 0; i <name.length; i++) {
                    Map<String, Object> map=new HashMap<String, Object>();
                    map.put("text",name[i]);
                    dataList.add(map);
                }
                View dialog = View.inflate(getContext(),R.layout.dialog_view_ship,null);
                GridView dialog_view_ship_gridview = (GridView) dialog.findViewById(R.id.dialog_view_ship_gridview);
                String[] from={"text"};
                int[] to={R.id.text};
                SimpleAdapter adapter=new SimpleAdapter(getContext(), dataList, R.layout.dialog_view_ship_item, from, to);
                dialog_view_ship_gridview.setAdapter(adapter);

                final AlertDialog alertdialog = new AlertDialog.Builder(getContext())
                        .setView(dialog).show();

                dialog_view_ship_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                            long arg3) {

                        switch (dataList.get(arg2).get("text").toString()){
                            case "简单火箭":
                                //打开简单火箭
                                PackageManager packageManager = getContext().getPackageManager();
                                Intent intent=new Intent();
                                intent =packageManager.getLaunchIntentForPackage("com.jundroo.simplerockets");
                                if(intent==null){
                                    Toast.makeText(getContext(), "未安装", Toast.LENGTH_LONG).show();
                                }else{
                                    getContext().startActivity(intent);
                                }
                                break;
                            case "视图编辑":
                                //打开视图编辑
                                Intent intent_activity = new Intent(getContext(), GalaxyDesignPoint.class);
                                getContext().startActivity(intent_activity);
                                galaxy_name = title;
                                break;
                            case "上传云端":
                                //上传云端
                                Toast.makeText(getContext(), "星系不能上传", Toast.LENGTH_SHORT).show();
                                break;
                            case "重新命名":
                                //载具分享

                                break;
                        }
                        alertdialog.dismiss();
                    }
                });
            }
        });
    }

    /**
     * ship
     */
    private static final Handler handler_ship = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            progressdialog.cancel();//完成
            try {
                final String data = msg.obj.toString();
                final int ID = msg.arg1;
                switch (msg.what){
                    case 0:
                        if(data!=null && data!="" && !data.equals("null") && !data.equals("") && data.length()>1){
                            //AlertDialog
                            new AlertDialog.Builder(getContext())
                                    .setMessage(data)
                                    .setTitle("载具")
                                    .setPositiveButton("复制", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog1, int which) {
                                            //置系统剪切板
                                            ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData mClipData = ClipData.newPlainText("Label",data);
                                            cm.setPrimaryClip(mClipData);
                                        }
                                    }).show();
                        }else {
                            Snackbar.make(recyclerview_ship, "载具上传失败", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                        break;
                    case 1:
                        if(data.indexOf("Ship")!=-1 && data!=null && data!="" && !data.equals("null") && !data.equals("") && data.length()>10){
                            final EditText ditetext_name = new EditText(getContext());
                            View dialog = View.inflate(getContext(), R.layout.dialog_view,null);
                            LinearLayout dialog_layout = (LinearLayout) dialog.findViewById(R.id.dialog_layout);
                            dialog_layout.addView(ditetext_name);

                            ditetext_name.setText(URLDecoder.decode(Util.substring(data,"name=\"","\"")+"", "UTF-8"));

                            new AlertDialog.Builder(getContext())
                                    .setView(dialog)
                                    .setTitle("命名")
                                    .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog1, int which) {
                                            File file =new File(path+"ships");
                                            //如果文件夹不存在则创建
                                            if  (!file .exists()  && !file .isDirectory())
                                            {
                                                file .mkdir();
                                            }
                                            FileUtil.write("ships/"+ditetext_name.getText().toString(),"<!--ID"+ ID +"-->\n"+data.replace(
                                                    "name=\""+ Util.substring(data,"name=\"","\"")+ "\"",//获取数据内的name
                                                    "name=\""+URLEncoder.encode(ditetext_name.getText().toString())+"\""));//将EditText内的数据转换为URL
                                        }
                                    }).show();
                        }else {
                            Snackbar.make(recyclerview_ship, "未找到该ID对应的载具", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                        break;
                }
                newship();//刷新载具
            }catch (Exception e){
                e.printStackTrace();
                Snackbar.make(recyclerview_ship, "错误", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    };

    /**
     * 传递Context
     * @return
     */
    public static Context getContext() {
        return TContext;
    }

    /**
     * 创建菜单Menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 菜单Menu事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_download://下载
                DownloadFile(null);
                return true;
            case R.id.menu_new://刷新
                newship();
                newgalaxy();
                return true;
            case R.id.menu_settings://设置

                return true;
            case R.id.menu_about://关于
                Intent intent = new Intent(MainActivity.this, AboutPoint.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 下载
     * @param ID
     */
    private void DownloadFile(String ID) {
        final EditText ditetext_id = new EditText(this);
        View dialog = View.inflate(MainActivity.this, R.layout.dialog_view,null);
        LinearLayout dialog_layout = (LinearLayout) dialog.findViewById(R.id.dialog_layout);
        dialog_layout.addView(ditetext_id);
        ditetext_id.setText(ID);
        ditetext_id.setHint("载具&沙盒ID或链接");
        new AlertDialog.Builder(MainActivity.this)
                .setView(dialog)
                .setTitle("存档")
                .setNegativeButton("载具", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //下载载具
                        if(!ditetext_id.getText().toString().equals("") && ditetext_id.getText().toString()!=null){
                            DownloadShip(ditetext_id);
                        }else {
                            //Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                            Snackbar.make(recyclerview_ship, "请勿输入空字符", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }
                })
                .setPositiveButton("沙盒", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        //下载沙盒
                        if(!ditetext_id.getText().toString().equals("") && ditetext_id.getText().toString()!=null){
                            DownloadSandbox(ditetext_id);
                        }else {
                            Snackbar.make(recyclerview_ship, "请勿输入空字符", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                }).show();
    }

    /**
     * 下载沙盒
     * @param ditetext_id
     */
    private void DownloadSandbox(final EditText ditetext_id) {
        try {
            //URL打开简单火箭，并下载沙盒
            String url = ditetext_id.getText().toString();
            url = url.replace("jundroo.com/ViewSandbox.html?id=","");
            url = url.replace("http://","");
            startActivity(new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("simplerockets://03" +url)));
            /**
             * PS:
             * 直接下载到本地的地址
             *
             */
        }catch (Exception e){
            Snackbar.make(recyclerview_ship, "未安装", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }
    /**
     * 下载载具
     * @param ditetext_id
     */
    private void DownloadShip(final EditText ditetext_id) {
        new AlertDialog.Builder(this)
                .setMessage("请选择下载方式")
                .setTitle("下载")
                .setNegativeButton("打开SR下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String url = ditetext_id.getText().toString();
                            url = url.replace("jundroo.com/ViewShip.html?id=","");
                            url = url.replace("http://","");
                            startActivity(new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("simplerockets://00" +url)));
                        }catch (Exception e){
                            Snackbar.make(recyclerview_ship, "未安装", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                })
                .setPositiveButton("下载到目录", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressdialog = ProgressDialog.show(getContext(), "提示", "正在下载...", false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String url = ditetext_id.getText().toString();
                                    if(url.indexOf("http")!=-1 | url.indexOf("com")!=-1 | url.length()>10){
                                        //链接
                                        if(url.indexOf("jundroo.com/ViewShip.html?id=")!=-1){
                                            url = url.replace("jundroo.com/ViewShip.html?id=","");
                                            url = url.replace("http://","");
                                        }else {
                                            url="233";
                                        }
                                    }
                                    final String data = HttpUtil.RequestHTTP("http://jundroo.com/service/SimpleRockets/DownloadRocket?id="+ url,"POST",null,null,null);
                                    Message mes = new Message();
                                    mes.obj = data+"";
                                    mes.arg1 = Integer.parseInt(url);
                                    mes.what = 1;
                                    handler_ship.sendMessage(mes);
                                }catch (Exception e){
                                    Message mes = new Message();
                                    mes.obj = "null";
                                    mes.arg1 = Integer.parseInt("0");
                                    mes.what = 1;
                                    handler_ship.sendMessage(mes);
                                }
                            }
                        }).start();
                    }
                }).show();
    }
}
