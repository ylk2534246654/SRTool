package com.yx.srtool.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yx.srtool.Activity.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by 雨夏 on 2019/4/19.
 */
public class FileUtil {
    /**
     *
     * @param filepath 文件名称
     * @return
     * @throws IOException
     */
    public static String read(String filepath) throws Exception {
        StringBuilder sb = new StringBuilder("");
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String path = MainActivity.path+filepath;
            //filename = context.getExternalCacheDir().getAbsolutePath() + java.io.File.separator + path;
            //打开文件输入流
            FileInputStream inputStream = new FileInputStream(path);

            Thread.sleep(200);
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            //读取文件内容
            while(len > 0){
                sb.append(new String(buffer,0,len));
                //继续将数据放到buffer中
                len = inputStream.read(buffer);
            }
            //关闭输入流
            inputStream.close();
        }
        return sb.toString();
    }
    /**
     *
     * @param filepath 文件名称File类型
     * @return
     * @throws IOException
     */
    public static String read2(File filepath) throws IOException {
        StringBuilder sb = new StringBuilder("");
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //打开文件输入流
            FileInputStream inputStream =new FileInputStream(filepath);

            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            //读取文件内容
            while(len > 0){
                sb.append(new String(buffer,0,len));
                //继续将数据放到buffer中
                len = inputStream.read(buffer);
            }
            //关闭输入流
            inputStream.close();
        }
        return sb.toString();
    }
    /**
     *
     * @param filepath 文件名称File类型
     * @return
     * @throws IOException
     */
    public static Bitmap read_Image(String filepath) throws IOException {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File mfile=new File(filepath);
            if (mfile.exists()) {//若该文件存在
                Bitmap bm = BitmapFactory.decodeFile(filepath);
                return bm;
            }
        }
        return null;
    }
    /**
     *
     * @param filepath	文件名称
     * @param data		需保存的内容
     */
    public static void write(String filepath,String data) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = MainActivity.path+filepath;
            //新建一个File对象，把我们要建的文件路径传进去。
            File file = new File(path);
            //判断文件是否存在，如果存在就删除。
            if (file.exists()) {
                file.delete();
            }
            try {
                //通过文件的对象file的createNewFile()方法来创建文件
                file.createNewFile();
                //新建一个FileOutputStream()，把文件的路径传进去
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                //给定一个字符串，将其转换成字节数组
                byte[] bytes = data.getBytes();
                //通过输出流对象写入字节数组
                fileOutputStream.write(bytes);
                //关流
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Message msg = new Message();
        handler_new.sendMessage(msg);
    }
    static final Handler handler_new = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //刷新列表
            MainActivity.newship();
            MainActivity.newgalaxy();
        }
    };
    /**
     *
     * @param path
     * @return
     */
    public static List<File> getFilesAllName(String path) {
        File file=new File(path);
        File[] files=file.listFiles();
        if (files == null){
            Log.e("error","空目录");
            return null;
        }
        List<File> s = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            s.add(files[i].	getAbsoluteFile());
        }
        return s;
    }
    /**
     * 不解压读取文件
     * @param file
     * @return
     * @throws Exception
     */
    public static String readZipFile(String file,String data) throws Exception {
        String README = "";
        ZipFile zf = new ZipFile(file);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
            if (ze.isDirectory()) {
            } else {
                //System.err.println("file - " + ze.getName() + " : "+ ze.getSize() + " bytes");
                if(ze.getName().equals(data)){
                    long size = ze.getSize();
                    if (size > 0) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
                        String line;
                        while ((line = br.readLine()) != null) {
                            if(README!=""){
                                README +=line+"\n";
                            }else {
                                README +=line;
                            }
                            Log.e("|",line);
                        }
                        br.close();
                    }
                }
            }
        }
        zin.closeEntry();
        return README;
    }

    /**
     * 文件拷贝 ： 要复制的目录下的所有非子目录(文件夹)文件拷贝
     * @param fromFile
     * @param toFile
     * @return
     */
    public static int CopySdcardFile(String fromFile, String toFile)
    {
        try{
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0)
            {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 0;
        } catch (Exception ex)
        {
            return -1;
        }
    }
    /**
     * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
     *
     * @param directory
     * context.getFilesDir() --> 清除/data/data/com.xxx.xxx/files下的内容
     */
    public static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}