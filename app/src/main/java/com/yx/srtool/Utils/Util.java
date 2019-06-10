package com.yx.srtool.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by Yx on 2019/4/22.
 */

public class Util {
    /**
     * 取字串符中间
     *
     * @param str
     * @param str1
     * @param str2
     */
    public static String substring(String str, String str1, String str2){
        int int_1 = str.indexOf(str1) + str1.length();// 获取右边指针
        int int_2 = str.indexOf(str2, int_1);// 获取左边指针
        String str3 = null;
        try {
            str3 = str.substring(int_1, int_2);// 根据指针获取指定值
        } catch (Exception e) {
            return null;
        }
        return str3;
    }
    /**
     * 字串符比对
     * @param str1
     * @return
     */
    public static boolean equals(String str, String str1) {
        if(str==null | str1==null) {
            return false;
        }

        str = str.replaceAll("\r", "").replaceAll("\n", "");
        if(str==str1) {
            return true;
        }
        if(str.equals(str1)) {
            return true;
        }
        return false;
    }
    private static String hexString = "0123456789abcdef";

    /*
     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
     */
    public static String encode(String str) {
        //根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        //将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f)));
        }
        return sb.toString();
    }
    public static String text_null(int str) {
        String str2 = "";
        for (int i = 0; i < str; i++) {
            str2 = str2 + "-";
        }
        return str2;
    }
    /*
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        //将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }
    /**
     * 解析ShipSprites.xml文件
     * @param result
     * @return
     * @throws Exception
     */
    public static List<Map<String,String>> parse_SpritesXML(String result) throws Exception {
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> map2 = new HashMap<>();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(result));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG://开始解析
                        if(Util.equals(nodeName,"TextureAtlas")){
                            map2.put("path",parser.getAttributeValue(null, "imagePath"));
                            list.add(map2);
                            //Log.e("TextureAtlas", "<"+nodeName +">"+parser.getAttributeValue(null, "imagePath"));
                        }else {
                            map2 = new HashMap<>();
                            map2.put("name",parser.getAttributeValue(null, "n"));
                            map2.put("x",parser.getAttributeValue(null, "x"));
                            map2.put("y",parser.getAttributeValue(null, "y"));
                            map2.put("w",parser.getAttributeValue(null, "w"));
                            map2.put("h",parser.getAttributeValue(null, "h"));
                            //Log.e("XML", "<"+nodeName +">"+"name:"+parser.getAttributeValue(null, "n"));
                        }
                        break;
                    case XmlPullParser.END_TAG://完成解析
                        if(Util.equals(nodeName,"TextureAtlas")){

                        }else {
                            list.add(map2);
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            return list;
    }
    /**
     * 解析PartList.xml文件
     * @param result
     * @return
     * @throws Exception
     */
    public static Map<String,Map<String,String>> parse_PartListXML(String result) throws Exception {
        //result = result.replace("\n","");

        Map<String,Map<String,String>> map = new HashMap<>();
        Map<String,String> map2 = new HashMap<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();

        InputStream is = new ByteArrayInputStream(result.getBytes());
        parser.setInput(is, "utf-8");
        //parser.setInput(new StringReader(result));
        int eventType = parser.getEventType();
        String ID = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String nodeName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG://开始解析

                    if(Util.equals(nodeName,"PartType")){
                        map2 = new HashMap<>();
                        //Log.e("PartType", "<"+nodeName +">");
                        ID = parser.getAttributeValue(null, "id");
                        map2.put("sprite",parser.getAttributeValue(null, "sprite"));//ID
                        map2.put("name",parser.getAttributeValue(null, "name"));//名称
                        map2.put("description",parser.getAttributeValue(null, "description"));//介绍
                        map2.put("sprite",parser.getAttributeValue(null, "sprite"));//图片路径
                        map2.put("type",parser.getAttributeValue(null, "type"));
                        map2.put("mass",parser.getAttributeValue(null, "mass"));
                        map2.put("width",parser.getAttributeValue(null, "width"));
                        map2.put("height",parser.getAttributeValue(null, "height"));
                        map2.put("hidden",parser.getAttributeValue(null, "hidden"));//是否能建造
                        //Log.e("TextureAtlas", "<"+nodeName +">"+parser.getAttributeValue(null, "imagePath"));
                    }
                    if(Util.equals(nodeName,"Tank")){
                        map2.put("fuel",parser.getAttributeValue(null, "fuel"));//燃料
                    }
                    break;
                case XmlPullParser.END_TAG://完成解析
                    if(Util.equals(nodeName,"PartType")){
                        map.put(ID,map2);
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return map;
    }
    /**
     * 解析载具文件
     * @param result
     * @return
     * @throws Exception
     */
    public static List<Map<String,String>> parse_shipXML(String result) throws Exception {
        //result = result.replace("\n","");
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> map2 = new HashMap<>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();

        InputStream is = new ByteArrayInputStream(result.getBytes());
        parser.setInput(is, "utf-8");
        //parser.setInput(new StringReader(result));
        int eventType = parser.getEventType();
        int Connections = 1 ;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String nodeName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG://开始解析
                    if(Util.equals(nodeName,"Part")){
                        map2 = new HashMap<>();
                        //Log.e("PartType", "<"+nodeName +">");
                        map2.put("partType",parser.getAttributeValue(null, "partType"));//名称
                        map2.put("id",parser.getAttributeValue(null, "id"));//ID
                        map2.put("x",parser.getAttributeValue(null, "x"));//X
                        map2.put("y",parser.getAttributeValue(null, "y"));//Y
                        map2.put("angle",parser.getAttributeValue(null, "angle"));//旋转
                        map2.put("angleV",parser.getAttributeValue(null, "angleV"));//图片路径
                        map2.put("editorAngle",parser.getAttributeValue(null, "editorAngle"));
                        map2.put("flippedX",parser.getAttributeValue(null, "flippedX"));
                        map2.put("flippedY",parser.getAttributeValue(null, "flippedY"));
                        map2.put("Connections",Connections+"");//焊接

                        Log.e("TextureAtlas", "<"+nodeName +">"+parser.getAttributeValue(null, "partType"));
                    }
                    if(Util.equals(nodeName,"Tank")){
                        map2.put("fuel",parser.getAttributeValue(null, "fuel"));//燃料
                    }
                    if(Util.equals(nodeName,"DisconnectedParts")){
                        Connections = 0;
                    }
                    break;
                case XmlPullParser.END_TAG://完成解析
                    if(Util.equals(nodeName,"Part")){
                        list.add(map2);
                    }
                    if(Util.equals(nodeName,"DisconnectedParts")){
                        Connections = 1;
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return list;
    }

    /**
     * 解析星系文件
     * @param result
     * @return
     * @throws Exception
     */
    public static List<Map<String,String>> parse_galaxyXML(String result) throws Exception {
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> map2 = new HashMap<String,String>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(result));

            String name = "";
            String gravity = "";
            String radius = "";
            String e = "";
            String w = "";
            String prograde = "";
            String a = "";
            String v = "";
            String Color_r = "";
            String Color_g = "";
            String Color_b = "";

            int Children = 0;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG://开始解析
                        if("Children".equals(nodeName)){
                            Children++;
                            Log.e(TAG, "Children++");
                        }
                        if ("Planet".equals(nodeName)){
                            Log.e(TAG, "Planet");
                            map2 = new HashMap<String,String>();
                            name = parser.getAttributeValue(null, "name");
                            gravity = parser.getAttributeValue(null, "gravity");
                            radius = parser.getAttributeValue(null, "radius");
                            if(parser.getAttributeValue(null, "mapColor")!=null){
                                String[] str = parser.getAttributeValue(null, "mapColor").split(",");
                                Color_r = str[0];
                                Color_g = str[1];
                                Color_b = str[2];
                            }
                            map2.put("Children",Children+"");
                            map2.put("name",name);
                            map2.put("gravity",gravity);
                            map2.put("radius",radius);
                            map2.put("Color_r",Color_r);
                            map2.put("Color_g",Color_g);
                            map2.put("Color_b",Color_b);
                            if(Children==0){//sun
                                list.add(map2);
                                map2 = new HashMap<String,String>();
                            }
                        }
                        if ("Orbit".equals(nodeName)){
                            w = parser.getAttributeValue(null, "w");
                            e = parser.getAttributeValue(null, "e");
                            prograde = parser.getAttributeValue(null, "prograde");
                            a = parser.getAttributeValue(null, "a");
                            v = parser.getAttributeValue(null, "v");

                            map2.put("w",w);
                            map2.put("e",e);
                            map2.put("prograde",prograde);
                            map2.put("a",a);
                            map2.put("v",v);
                            if(Children==1){
                            }
                            list.add(map2);
                            map2 = new HashMap<String,String>();
                        }
                        //Log.e(TAG, "<"+nodeName +">");
                        Log.e(TAG, "<"+nodeName +">"+"name:"+parser.getAttributeValue(null, "name"));
                        break;
                    case XmlPullParser.END_TAG://完成解析
                        Log.e(TAG, "</"+nodeName +">");
                        if("Children".equals(nodeName)){
                            Children--;
                            Log.e(TAG, "Children--");
                        }
                        if ("Planet".equals(nodeName)){
                            if(map2.get("name")!="" && map2.get("name")!=null){
                                list.add(map2);
                                map2 = new HashMap<String,String>();
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    private static int mapi=0,shangyige=0,Children=0;
    private static String Color_r,Color_g,Color_b;
    public static Map<Integer,Map<String,String>> parseXMLWithPull(String result) throws Exception {
        Map<Integer,Map<String,String>> map = new HashMap<>();
        Map<String,String> map2 = new HashMap<String,String>();
        mapi=0;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(result));
            String name = "";
            String gravity = "";
            String radius = "";
            String e = "";
            String w = "";
            String prograde = "";
            String a = "";
            String v = "";
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG://开始解析
                        if("Children".equals(nodeName)){
                            Children++;
                            Log.e(TAG, "Children++");
                        }

                        if ("Planet".equals(nodeName)){
                            Log.e(TAG, "Planet");
                            map2 = new HashMap<String,String>();
                            name = parser.getAttributeValue(null, "name");
                            gravity = parser.getAttributeValue(null, "gravity");
                            radius = parser.getAttributeValue(null, "radius");
                            if(parser.getAttributeValue(null, "mapColor")!=null){
                                String[] str = parser.getAttributeValue(null, "mapColor").split(",");
                                Color_r = str[0];
                                Color_g = str[1];
                                Color_b = str[2];
                            }
                            map2.put("Children",Children+"");

                            map2.put("name",name);
                            map2.put("gravity",gravity);
                            map2.put("radius",radius);
                            map2.put("Color_r",Color_r);
                            map2.put("Color_g",Color_g);
                            map2.put("Color_b",Color_b);
                            if(Children==0){//sun
                                mapi++;
                                map2.put("i",mapi+"");
                                map.put(mapi,map2);
                                map2 = new HashMap<String,String>();
                            }
                        }
                        if ("Orbit".equals(nodeName)){

                            w = parser.getAttributeValue(null, "w");
                            e = parser.getAttributeValue(null, "e");
                            prograde = parser.getAttributeValue(null, "prograde");
                            a = parser.getAttributeValue(null, "a");
                            v = parser.getAttributeValue(null, "v");

                            map2.put("w",w);
                            map2.put("e",e);
                            map2.put("prograde",prograde);
                            map2.put("a",a);
                            map2.put("v",v);
                            mapi=mapi+2;
                            if(Children==1){
                                shangyige = mapi;
                            }
                            map2.put("i",shangyige+"");
                            map.put(mapi,map2);
                            map2 = new HashMap<String,String>();
                        }
                        //Log.e(TAG, "<"+nodeName +">");
                        Log.e(TAG, "<"+nodeName +">"+"name:"+parser.getAttributeValue(null, "name"));
                        break;
                    case XmlPullParser.END_TAG://完成解析
                        Log.e(TAG, "</"+nodeName +">");
                        if("Children".equals(nodeName)){
                            Children--;
                            Log.e(TAG, "Children--");
                        }

                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    public static Bitmap tintBitmap(Bitmap inBitmap , int tintColor) {
        if (inBitmap == null) {
            return null;
        }
        Bitmap outBitmap = Bitmap.createBitmap (inBitmap.getWidth(), inBitmap.getHeight() , inBitmap.getConfig());
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setColorFilter( new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)) ;
        canvas.drawBitmap(inBitmap , 0, 0, paint) ;
        return outBitmap ;
    }
}