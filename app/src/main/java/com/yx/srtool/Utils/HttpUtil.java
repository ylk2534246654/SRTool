package com.yx.srtool.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

/**
 * Created by Yx on 2019/4/21.
 */

public class HttpUtil {
    /**
     * Http请求
     * @param httpurl		请求地址
     * @param request	请求协议
     * @param data		请求参数
     * @param RequestProperty	请求头
     * @param chaset	字符集
     * @return
     */
    public static String RequestHTTP(String httpurl, String request, String data, Map<String,String> RequestProperty, String chaset) {
        if(chaset==null){
            chaset ="UTF-8";
        }
        request = request.toUpperCase();
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            // 设置连接方式
            connection.setRequestMethod(request);
            //至少要设置的两个请求头
            //connection.setRequestProperty("User-Agent", WebSettings.getDefaultUserAgent(APP.getContext()));
            connection.setRequestProperty("Charset", chaset);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:42.0) Gecko/20100101 Firefox/42.0");

            if(RequestProperty != null) {
                for(Map.Entry<String,String> entry : RequestProperty.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                    //System.out.println(entry.getKey()+":"+entry.getValue());
                }
            }
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            if(data!=null && request.equals("POST")) {
                connection.setRequestProperty("Content-Length", data.length()+"");
                // 通过连接对象获取一个输出流
                OutputStream outputStream = connection.getOutputStream();
                // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
                outputStream.write(data.getBytes());
            }
            //获得结果码 200:请求成功
            if(connection.getResponseCode() ==200){
                // 通过打开的连接读取的输入流,获取html数据
                InputStream is = connection.getInputStream();
                return dealResponseResult(is,chaset);
            }else {
                //请求失败
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    /**
     * 处理服务器的响应结果（将输入流转化成字符串）
     * @param inputStream	服务器的响应输入流
     * @param chaset		字符集
     * @return
     */
    public static String dealResponseResult(InputStream inputStream,String chaset) {


        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
            resultData = new String(byteArrayOutputStream.toByteArray(),chaset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultData;
    }

}