package com.yx.srtool.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yx.srtool.R;
import com.yx.srtool.Utils.FileUtil;
import com.yx.srtool.Utils.UriToPathUtil;
import com.yx.srtool.i.MainPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.html.HtmlPlugin;
import ru.noties.markwon.image.ImagesPlugin;

/**
 * Created by Yx on 2019/4/25.
 */

public class ShipActivity extends AppCompatActivity {
    //String path;
    private ProgressDialog Dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        if(Build.VERSION.SDK_INT>=23){
            if(Settings.canDrawOverlays(this))
            {//有悬浮窗权限开启服务绑定 绑定权限
                createToucher();
            }else{//没有悬浮窗权限m,去开启悬浮窗权限
                try{
                    //没有权限
                    Toast.makeText(ShipActivity.this, "当前无权限，请授权！", Toast.LENGTH_SHORT).show();
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
    private void createToucher() {
        FileUtil.deleteFilesByDirectory(getExternalFilesDir(null));
        //获取传递值
        final Intent intent = getIntent();
        String action = intent.getAction();
        if(intent.ACTION_VIEW.equals(action)){
            Toast.makeText(this, "正在解析存档...", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String path = UriToPathUtil.getFilePathFromURI(ShipActivity.this, intent.getData());
                        //Log.e("接收","uri："+path);
                        //path = java.net.URLDecoder.decode(intent.getDataString(),   "utf-8");
                        //path = Util.substring(path+"<end>","/storage","<end>");
                        //path = "storage"+path;
                        //path = path.replace("file:/","").replace("content:/","");
                        Message mes = new Message();
                        mes.obj = path+"";
                        handler_mod.sendMessage(mes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }
    private final Handler handler_mod= new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            final String path = msg.obj.toString();

            //初始化
            LayoutInflater layoutInflater = LayoutInflater.from(ShipActivity.this);
            //dialog_view
            View dialog_view = layoutInflater.inflate(R.layout.activity_mod, null);
            //LinearLayout chat_about_LinearLayout = (LinearLayout) activity_mod.findViewById(R.id.chat_about_LinearLayout);
            //ScrollView scrollView = (ScrollView) activity_mod.findViewById(R.id.about_ScrollView);
            TextView textView = (TextView) dialog_view.findViewById(R.id.textView);



            String README ="## 此文件并不是简单火箭存档";
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext())
                    .setView(dialog_view)
                    .setMessage("存档管理");
            final AlertDialog dialog = builder.create();
            Button openSR = (Button) dialog_view.findViewById(R.id.openSR);

            try {
                String data = FileUtil.read(path);
                if(data.indexOf("Ship")!=-1){
                    README = "此文件为简单火箭存档\n无介绍";
                }else {
                    openSR.setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(ShipActivity.this, e+"", Toast.LENGTH_SHORT).show();
            }
            Markwon markwon = Markwon.builder(ShipActivity.this)
                    .usePlugin(HtmlPlugin.create())//HTML
                    .usePlugin(ImagesPlugin.create(ShipActivity.this))//内核
                    .build();
            markwon.setMarkdown(textView, README);


            openSR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();//关闭
                    FileUtil.CopySdcardFile(path, MainActivity.path+"ships/"+FileUtil.getFileNameWithSuffix(path));
                    Intent intent_activity =
                            new Intent(ShipActivity.this,
                            MainPoint.class);
                    startActivity(intent_activity);
                    /*
                //如果API是24以上，打开注释
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName("com.jundroo.simplerockets", "com.jundroo.simplerockets.MainActivity");
                Uri uri=null;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    File mod = new File(path);
                    uri = FileProvider.getUriForFile(ModActivity.this, BuildConfig.APPLICATION_ID+ ".fileprovider", mod);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setData(uri);
                } else{
                    uri = Uri.fromFile(new File(path));
                    intent.setData(uri);
                }
                Log.e("uri",uri+"");
                startActivityForResult(intent, 1);
                //startActivity(intent);
                */
                    /*
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setClassName("com.jundroo.simplerockets", "com.jundroo.simplerockets.MainActivity");
                    Uri uri = Uri.fromFile(new File(path));
                    intent.setData(uri);
                    Log.e("uri",uri+"");
                    startActivityForResult(intent, 1);
                    dialog.cancel();//关闭
                    //startActivity(intent);
                    finish();
                    */
                }
            });
            /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//https://blog.csdn.net/qq_23374873/article/details/80718948
            //关于 8.0以上
            dialog.getWindow().setType(
                    (WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
        }else {
            //关于 8.0以下
            dialog.getWindow().setType(
                    (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }
        */
        //关于 8.0以下
            dialog.getWindow().setType(
                    (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
                public void run() {
                    dialog.show();
                }
            });
        }
    };
}
