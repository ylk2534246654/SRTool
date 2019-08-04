package com.yx.srtool.View;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.yx.srtool.Activity.RocketDesignActivity;
import com.yx.srtool.Activity.MainActivity;
import com.yx.srtool.Adapter.GalaxyAdapter;
import com.yx.srtool.Utils.FileUtil;
import com.yx.srtool.Utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yx on 2019/4/22.
 */

public class RocketView extends View{
    private static Bitmap bitmap;
    private Paint Paint_Line//Paint_Line线
            ,Paint_Texe//Paint_Texe文字
            ,Paint_graph;//Paint_graph图形

    private Map<String, Bitmap> Sprites_Bitmap;
    private static List<Map<String,String>> Ships_List;
    private Map<String,Map<String,String>> PartList_xml;

    public static String ship;//载具数据
    public static boolean selectMOVE;
    public Bitmap Background;//背景图

    private float Multiple = 1//放大倍数
            ,eventX0 = getWidth()/2
            ,eventY0 = getHeight()/2
            ,eventX_MOVE,eventY_MOVE
            ,event_drag_x//拖动x
            ,event_drag_y;//拖动y

    private double nLenStart;//上一次放大状态保存
    private int event_drag;//是否是拖动状态，1：拖动状态，0：没有拖动
    public static int PartList_ID = -1;//ID
    private boolean parameter_box_visible//框可视状态
            ,Already_parameter_box_visible//存储框可视状态
            ,grid_visible;//网格可视状态

    private int TouchEvent;//触摸事件，0：按下，1：抬起，2：移动
    private boolean click=false//点击
            ,Longclick=false;//长按
    public RocketView(Context context) {
        super(context);
        init();
    }
    public RocketView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public RocketView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        try {
            //Sprites_Bitmap : 分割精灵位图
            Sprites_Bitmap = getStringBitmapMap();
            //获取xml文件
            String sprites_xml = FileUtil.read(MainActivity.path+"PartList.xml");
            //解析PartList.xml文件，XML
            PartList_xml = Util.parse_PartListXML(sprites_xml);
            //获取载具文件
            File file = new File(MainActivity.path+"ships/"+ MainActivity.ship_name);
            //获取文件内容
            ship = FileUtil.read2(file);
            initxml();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //画线笔
        Paint_Line = new Paint();
        Paint_Line.setColor(Color.rgb(230,230,250));
        Paint_Line.setStrokeWidth(3);
        Paint_Line.setStyle(Paint.Style.STROKE);
        Paint_Line.setTextSize(20);
        Paint_Line.setAntiAlias(true);
        Paint_Line.setAlpha(80);
        //写字笔
        Paint_Texe = new Paint();
        Paint_Texe.setColor(Color.rgb(230,230,250));
        Paint_Texe.setTextSize(30);
        Paint_Texe.setAntiAlias(true);
        //图形笔
        Paint_graph = new Paint();
        Paint_graph.setAntiAlias(true);
        //背景
        try {
            Background = FileUtil.read_Image(MainActivity.path+"DialogBackground.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initxml() throws Exception {
        //解析载具文件，XML
        Ships_List = Util.parse_shipXML(ship);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initDraw(canvas);//初始化画布
        startDraw(canvas);


        bitmap = Bitmap.createBitmap(canvas.getWidth(),canvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(bitmap);
        //移动中心坐标
        mCanvas.translate(getWidth()/2,getHeight()/2);
        //缩放
        mCanvas.scale(Multiple,Multiple);
        startImage(mCanvas);
        super.onDraw(canvas);
    }

    private void initDraw(Canvas canvas) {

        //canvas.scale(1,-1);// 翻转Y轴
        //设置背景
        Bitmap Back = Bitmap.createScaledBitmap(Background, getWidth(), getHeight(), true);
        canvas.drawBitmap(Back, 0, 0, null);
        //移动中心坐标
        canvas.translate(getWidth()/2,getHeight()/2);
        //渲染
        //设置线段颜色
        Paint_Line.setColor(Color.rgb(179,227,1));
        //设置线段宽度
        Paint_Line.setStrokeWidth(3);
        //画出竖线
        canvas.drawLine(-getWidth()/2, eventY0*Multiple, getWidth()/2, eventY0*Multiple, Paint_Line);
        //画出横线
        canvas.drawLine(eventX0*Multiple, -getHeight()/2, eventX0*Multiple, getHeight()/2, Paint_Line);
        //判断网格是否可见
        if (grid_visible){
            //设置网格宽度
            Paint_Line.setStrokeWidth(1);
            //判断是否小于O，转为正数
            float ls_eventY;
            if(eventY0<0){
                ls_eventY = -eventY0;
            }else {
                ls_eventY = eventY0;
            }
            float ls_eventX;
            if(eventX0 <0){
                ls_eventX = -eventX0;
            }else {
                ls_eventX = eventX0;
            }
            //Log.e("eventY0",eventY0+"");
            for (int i = 0;i<(ls_eventY+getWidth()/Multiple)/30;i++){
                canvas.drawLine(-getWidth()/2, (eventY0-i*30)*Multiple, getWidth()/2, (eventY0-i*30)*Multiple, Paint_Line);
                canvas.drawLine(-getWidth()/2, (eventY0+i*30)*Multiple, getWidth()/2, (eventY0+i*30)*Multiple, Paint_Line);
            }
            for (int i = 0;i<(ls_eventX+getHeight()/Multiple)/30;i++){
                canvas.drawLine((eventX0-i*30)*Multiple, -getHeight()/2, (eventX0-i*30)*Multiple, getHeight()/2, Paint_Line);
                canvas.drawLine((eventX0+i*30)*Multiple, -getHeight()/2, (eventX0+i*30)*Multiple, getHeight()/2, Paint_Line);
            }
        }

        //缩放
        canvas.scale(Multiple,Multiple);
    }

    private void startDraw(Canvas canvas) {
        try {
            //计算现在的中心坐标
            float ls_event_drag_x,ls_event_drag_y;
            if(selectMOVE){
                ls_event_drag_x = (float) ((int)(((event_drag_x - getWidth()/2)/Multiple)/30)*30);
                ls_event_drag_y = (float) ((int)(((event_drag_y - getHeight()/2)/Multiple)/30)*30);
            }else {
                ls_event_drag_x = (event_drag_x - getWidth()/2)/Multiple;
                ls_event_drag_y = (event_drag_y - getHeight()/2)/Multiple;
            }
            //设置框初始化，不可视
            parameter_box_visible=false;
            for(int i =0;i<Ships_List.size();i++) {
                Map<String,String> Ships_Parameter = Ships_List.get(i);
                Map<String,String> Ships_png_path = PartList_xml.get(Ships_Parameter.get("partType"));
                Bitmap Ships_Parameter_Bitmap,Ships_Parameter_Bitmap2 = null,Ships_Parameter_Bitmap3 = null;
                boolean landerleg = false;
                //判断当前是否是着陆架
                if(Ships_png_path.get("sprite").toLowerCase().indexOf("landerlegpreview.png")!=-1){
                    Ships_Parameter_Bitmap = Sprites_Bitmap.get("landerlegjoint.png");//头
                    Ships_Parameter_Bitmap2 = Sprites_Bitmap.get("landerleglower.png");//支
                    Ships_Parameter_Bitmap3 = Sprites_Bitmap.get("landerlegupper.png");//管
                    landerleg = true;
                }else {
                    Ships_Parameter_Bitmap = Sprites_Bitmap.get(Ships_png_path.get("sprite").toLowerCase());//Bitmap,png格式
                }

                try {
                    //计算角度
                    float angle = (float) -(Float.parseFloat(Ships_Parameter.get("angle"))*((90/(Math.PI/2))));
                    //图片中心坐标
                    int offsetX = Ships_Parameter_Bitmap.getWidth() / 2;
                    int offsetY = Ships_Parameter_Bitmap.getHeight() / 2;
                    //图片变形
                    Matrix matrix = new Matrix();
                    Matrix matrix2 = new Matrix();
                    //旋转： 往自身的移动半宽，半长
                    matrix.postTranslate(-offsetX, -offsetY);
                    //如果当前为着陆架
                    if(landerleg){
                        matrix2.postTranslate(- Ships_Parameter_Bitmap2.getWidth() / 2, - Ships_Parameter_Bitmap2.getWidth() / 2);
                        matrix2.postRotate(180+angle);
                    }
                    //旋转
                    matrix.postRotate(angle);
                    //图片翻转
                    if(Ships_Parameter.get("flippedX")!= null && Ships_Parameter.get("flippedY")!= null){
                        if(Ships_Parameter.get("flippedX").equals("1")){
                            matrix.postScale(-1, -1);
                        }
                        if(Ships_Parameter.get("flippedY").equals("1")){
                            matrix.postScale(-1, -1);
                        }
                    }
                    Paint paint = new Paint();
                    //判断当前是否未接入
                    if(Ships_Parameter.get("Connections").equals("0")){
                        //透明度
                        paint.setAlpha( 100 );
                    }
                    //图片x,y
                    float Bitmap_x,Bitmap_y;
                    //判断当前是否被拖动，id对应，是否长按点击
                    if(event_drag == 1 && PartList_ID == i && Longclick){
                        Bitmap_x = ls_event_drag_x;
                        Bitmap_y = ls_event_drag_y-(int)(300/Multiple/30)*30;
                    }else {
                        Bitmap_x = eventX0+2*(Float.parseFloat(Ships_Parameter.get("x"))*30);
                        Bitmap_y = eventY0-2*(Float.parseFloat(Ships_Parameter.get("y"))*30);
                    }
                    //判断是否为着陆架
                    if(landerleg){
                        //设置Bitmap坐标
                        matrix2.postTranslate(Bitmap_x, Bitmap_y);
                        //渲染出图片
                        canvas.drawBitmap(Ships_Parameter_Bitmap2, matrix2, paint);
                        canvas.drawBitmap(Ships_Parameter_Bitmap3, matrix2, paint);
                    }
                    //设置Bitmap坐标
                    matrix.postTranslate(Bitmap_x, Bitmap_y);
                    //渲染出图片
                    canvas.drawBitmap(Ships_Parameter_Bitmap, matrix, paint);
                    //取最短的
                    float fanwei;
                    if(offsetX>offsetY){
                        fanwei =offsetY;
                    }else {
                        fanwei =offsetX;
                    }
                    //计算是否在范围内
                    float ls_event_drag_y2 = ls_event_drag_y-(int)(300/Multiple/30)*30;
                    if((ls_event_drag_x + fanwei)>Bitmap_x && Bitmap_x>(ls_event_drag_x - fanwei) && (ls_event_drag_y + fanwei)>Bitmap_y && Bitmap_y>(ls_event_drag_y - fanwei) &&event_drag != -1){
                        //如果id为无
                        if(PartList_ID == -1){
                            //设置赋值id
                            PartList_ID = i;
                            //设置当前是拖动状态
                            event_drag = 1;
                        }else if(PartList_ID == i){//否则如果ID
                            //渲染颜色
                            canvas.drawBitmap(Util.tintBitmap(Ships_Parameter_Bitmap,0x96B3E301), matrix, paint);
                            //更新数据
                            String ship_angle = Util.substring(ship,"id=\""+Ships_Parameter.get("id")+"\" x=\""+Ships_Parameter.get("x")+"\" y=\""+Ships_Parameter.get("y")+"\" angle=\"","\"");
                            ship = ship.replace("id=\""+Ships_Parameter.get("id")+"\" x=\""+Ships_Parameter.get("x")+"\" y=\""+Ships_Parameter.get("y")+"\" angle=\""+ship_angle+"\"",
                                    "id=\""+Ships_Parameter.get("id")+"\" x=\""+Ships_Parameter.get("x")+"\" y=\""+Ships_Parameter.get("y")+"\" angle=\""+Ships_Parameter.get("angle")+"\"");
//                            //在四周渲染出箭头
//                            //右边的
//                            canvas.drawCircle(Bitmap_x+200, Bitmap_y, 50, Paint_graph);
//                            //左边的
//                            canvas.drawCircle(Bitmap_x-200, Bitmap_y, 50, Paint_graph);
//                            //上边的
//                            canvas.drawCircle(Bitmap_x, Bitmap_y-200, 50, Paint_graph);
                            //下边的
                            //Paint_graph.setColor(0x96B3E301);
                            //canvas.drawCircle(Bitmap_x, Bitmap_y+300/Multiple, 50/Multiple, Paint_graph);
                            parameter_box_visible=true;
                            if (TouchEvent == 1 && !Already_parameter_box_visible){
                                //设置框可视
                                Already_parameter_box_visible = true;
                                //弹出框
                                RocketDesignActivity.click_focus(Ships_Parameter.get("partType"),Ships_Parameter.get("id"),angle);
                            }

                            //invalidate();
                        }

                    }else if((ls_event_drag_x + fanwei)>Bitmap_x && Bitmap_x>(ls_event_drag_x - fanwei) && (ls_event_drag_y2 + fanwei)>Bitmap_y && Bitmap_y>(ls_event_drag_y2 - fanwei) &&event_drag != -1 && PartList_ID == i){
                        //渲染颜色
                        canvas.drawBitmap(Util.tintBitmap(Ships_Parameter_Bitmap,0x96B3E301), matrix, paint);
                        //更新数据
                        String ship_angle = Util.substring(ship,"id=\""+Ships_Parameter.get("id")+"\" x=\""+Ships_Parameter.get("x")+"\" y=\""+Ships_Parameter.get("y")+"\" angle=\"","\"");
                        ship = ship.replace("id=\""+Ships_Parameter.get("id")+"\" x=\""+Ships_Parameter.get("x")+"\" y=\""+Ships_Parameter.get("y")+"\" angle=\""+ship_angle+"\"",
                                "id=\""+Ships_Parameter.get("id")+"\" x=\""+Ships_Parameter.get("x")+"\" y=\""+Ships_Parameter.get("y")+"\" angle=\""+Ships_Parameter.get("angle")+"\"");
//                            //在四周渲染出箭头
//                            //右边的
//                            canvas.drawCircle(Bitmap_x+200, Bitmap_y, 50, Paint_graph);
//                            //左边的
//                            canvas.drawCircle(Bitmap_x-200, Bitmap_y, 50, Paint_graph);
//                            //上边的
//                            canvas.drawCircle(Bitmap_x, Bitmap_y-200, 50, Paint_graph);
                        //下边的
                        Paint_graph.setColor(0x96B3E301);
                        canvas.drawCircle(Bitmap_x, Bitmap_y+(int)(300/Multiple/30)*30, 50/Multiple, Paint_graph);
                        if(selectMOVE){
                            eventX0 = (int)(eventX0/30)*30;
                            eventY0 = (int)(eventY0/30)*30;
                        }
                        grid_visible=true;
                        //框开启
                        parameter_box_visible=true;
                        if (TouchEvent == 1 && !Already_parameter_box_visible){
                            //设置框可视
                            Already_parameter_box_visible = true;
                            //弹出框
                            RocketDesignActivity.click_focus(Ships_Parameter.get("partType"),Ships_Parameter.get("id"),angle);
                        }
                    }else if(PartList_ID == i){
                        grid_visible=false;
                        event_drag = 0;
                        //设置ID为无
                        PartList_ID = -1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //关闭框
        if(!parameter_box_visible){
            RocketDesignActivity.click_notfocus();
            Already_parameter_box_visible=false;
        }
    }


    private void startImage(Canvas canvas) {
        try {
            for(int i =0;i<Ships_List.size();i++) {
                Map<String,String> Ships_Parameter = Ships_List.get(i);
                Map<String,String> Ships_png_path = PartList_xml.get(Ships_Parameter.get("partType"));
                Bitmap Ships_Parameter_Bitmap,Ships_Parameter_Bitmap2 = null,Ships_Parameter_Bitmap3 = null;
                boolean landerleg = false;
                //判断当前是否是着陆架
                if(Ships_png_path.get("sprite").toLowerCase().indexOf("landerlegpreview.png")!=-1){
                    Ships_Parameter_Bitmap = Sprites_Bitmap.get("landerlegjoint.png");//头
                    Ships_Parameter_Bitmap2 = Sprites_Bitmap.get("landerleglower.png");//支
                    Ships_Parameter_Bitmap3 = Sprites_Bitmap.get("landerlegupper.png");//管
                    landerleg = true;
                }else {
                    Ships_Parameter_Bitmap = Sprites_Bitmap.get(Ships_png_path.get("sprite").toLowerCase());//Bitmap,png格式
                }

                try {
                    //计算角度
                    float angle = (float) -(Float.parseFloat(Ships_Parameter.get("angle"))*((90/(Math.PI/2))));
                    //图片中心坐标
                    int offsetX = Ships_Parameter_Bitmap.getWidth() / 2;
                    int offsetY = Ships_Parameter_Bitmap.getHeight() / 2;
                    //图片变形
                    Matrix matrix = new Matrix();
                    Matrix matrix2 = new Matrix();
                    //旋转： 往自身的移动半宽，半长
                    matrix.postTranslate(-offsetX, -offsetY);
                    //如果当前为着陆架
                    if(landerleg){
                        matrix2.postTranslate(- Ships_Parameter_Bitmap2.getWidth() / 2, - Ships_Parameter_Bitmap2.getWidth() / 2);
                        matrix2.postRotate(180+angle);
                    }
                    //旋转
                    matrix.postRotate(angle);
                    //图片翻转
                    if(Ships_Parameter.get("flippedX")!= null && Ships_Parameter.get("flippedY")!= null){
                        if(Ships_Parameter.get("flippedX").equals("1")){
                            matrix.postScale(-1, -1);
                        }
                        if(Ships_Parameter.get("flippedY").equals("1")){
                            matrix.postScale(-1, -1);
                        }
                    }
                    //判断当前是否未接入
                    if(Ships_Parameter.get("Connections").equals("0")){
                        //未接入组件
                        //透明度
                        //paint.setAlpha( 100 );
                    }else {
                        Paint paint = new Paint();
                        //图片x,y
                        float Bitmap_x = eventX0+2*(Float.parseFloat(Ships_Parameter.get("x"))*30);
                        float Bitmap_y = eventY0-2*(Float.parseFloat(Ships_Parameter.get("y"))*30);
                        //判断是否为着陆架
                        if(landerleg){
                            //设置Bitmap坐标
                            matrix2.postTranslate(Bitmap_x, Bitmap_y);
                            //渲染出图片
                            canvas.drawBitmap(Ships_Parameter_Bitmap2, matrix2, paint);
                            canvas.drawBitmap(Ships_Parameter_Bitmap3, matrix2, paint);
                        }
                        //设置Bitmap坐标
                        matrix.postTranslate(Bitmap_x, Bitmap_y);
                        //渲染出图片
                        canvas.drawBitmap(Ships_Parameter_Bitmap, matrix, paint);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 屏幕事件
     * 放大参考https://blog.csdn.net/csdnzouqi/article/details/79853109
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pCount = event.getPointerCount();// 触摸设备时手指的数量
        // 获取触屏动作。比如：按下、移动和抬起等手势动作
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://0按下
                TouchEvent = 0;
                switch (pCount) {//手指数量
                    case 1:
                        eventX_MOVE = event.getX()/Multiple;
                        eventY_MOVE = event.getY()/Multiple;
                        event_drag_x = event.getX();
                        event_drag_y = event.getY();
                        //执行长按
                        handler_time.postDelayed(runnable,200);
                        break;
                    case 2:
                        event_drag = 0;
                        break;
                }
                break;
            case MotionEvent.ACTION_UP://1抬起
                TouchEvent = 1;

                //抬起结束
                switch (pCount) {//手指数量
                    case 1:
                        //抬起保存
                        if(event_drag == 1 && PartList_ID!=-1 && Longclick){
                            //抬起保存
                            Map<String,String> Ships_Parameter = Ships_List.get(PartList_ID);
                            float ls_event_drag_x,ls_event_drag_y;
                            if(selectMOVE){
                                ls_event_drag_x = (float) ((int)(((event_drag_x - getWidth()/2)/Multiple)/30)*30);
                                ls_event_drag_y = (float) ((int)(((event_drag_y - getHeight()/2)/Multiple)/30)*30);
                            }else {
                                ls_event_drag_x = (event_drag_x - getWidth()/2)/Multiple;
                                ls_event_drag_y = (event_drag_y - getHeight()/2)/Multiple;
                            }


                            ls_event_drag_y = ls_event_drag_y-(int)(300/Multiple/30)*30;

                            float x = ((ls_event_drag_x-eventX0)/2)/30;
                            float y = -((ls_event_drag_y-eventY0)/2)/30;
                            String ship_angle = Util.substring(ship,"id=\""+Ships_Parameter.get("id")+"\" x=\""+Ships_Parameter.get("x")+"\" y=\""+Ships_Parameter.get("y")+"\" angle=\"","\"");
                            ship = ship.replace("id=\""+Ships_Parameter.get("id")+"\" x=\""+Ships_Parameter.get("x")+"\" y=\""+Ships_Parameter.get("y")+"\" angle=\""+ship_angle+"\"",
                                    "id=\""+Ships_Parameter.get("id")+"\" x=\""+x+"\" y=\""+y+"\" angle=\""+Ships_Parameter.get("angle")+"\"");
                            //Log.e("ship_angle",ship_angle);
                            Ships_Parameter.put("x",x+"");
                            Ships_Parameter.put("y",y+"");
                            Ships_List.set(PartList_ID,Ships_Parameter);
                        }
                        event_drag = 0;
                        break;
                }
                Longclick=false;
                break;
            case MotionEvent.ACTION_MOVE://2移动
                TouchEvent = 2;
                //放大
                switch (pCount) {//手指数量
                    case 1:
                        //如果当前是拖动状态
                        if(event_drag == 1){
                            //如果是长按点击
                            if(Longclick){
                                //设置坐标
                                event_drag_x = event.getX();
                                event_drag_y = event.getY();
                            }
                        }else {
                            //取消拖动
                            event_drag = -1;
                            if(event.getX()/Multiple>eventX_MOVE){
                                eventX0=eventX0+(event.getX()/Multiple-eventX_MOVE);
                            }else if(event.getX()/Multiple<eventX_MOVE){
                                eventX0=eventX0-(eventX_MOVE-event.getX()/Multiple);
                            }
                            if (event.getY()/Multiple>eventY_MOVE) {
                                eventY0=eventY0+(event.getY()/Multiple-eventY_MOVE);
                            }else if (event.getY()/Multiple<eventY_MOVE) {
                                eventY0=eventY0-(eventY_MOVE-event.getY()/Multiple);
                            }

                        }
                        break;
                    case 2:
                        //取消拖动
                        event_drag = -1;
                        // 获取按下时候两个坐标的x轴的水平距离，取绝对值
                        int xLen = Math.abs((int)event.getX(0) - (int)event.getX(1));
                        // 获取按下时候两个坐标的y轴的水平距离，取绝对值
                        int yLen = Math.abs((int)event.getY(0) - (int)event.getY(1));
                        // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指刚按下时的直线距离
                        int value = (int) Math.sqrt((double) xLen * xLen + (double) yLen * yLen);

                        if(nLenStart!=0){
                            if(nLenStart>value ) {
                                Multiple -= Multiple/10;
                            }else if(nLenStart<value) {
                                Multiple += Multiple/10;
                            }
                            nLenStart = value;
                        }else {
                            nLenStart = value;
                        }
                        break;
                }
                eventX_MOVE = event.getX()/Multiple;
                eventY_MOVE = event.getY()/Multiple;
                break;
        }
        invalidate();
        return true;
    }

    Handler handler_time = new Handler();                //时间线程
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(TouchEvent == 1){//在200毫秒之内抬起了
                click=true;
                Longclick = false;
                //Log.e("Event","点击");
            }else if(TouchEvent == 0 | TouchEvent == 2 ){
                Longclick = true;
                click=false;
                //Log.e("Event","长按");
            }
        }
    };
    /**
     * 分割精灵位图
     * @return
     * @throws Exception
     */
    private Map<String, Bitmap> getStringBitmapMap() throws Exception {
        Map<String,Bitmap> Sprites_Bitmap = new HashMap<>();
        //获取xml文件
        String sprites = FileUtil.read(MainActivity.path+"ShipSprites.xml");
        //xml解析
        List<Map<String,String>> list = Util.parse_SpritesXML(sprites);
        //根据xml内的参数分割图片
        Bitmap all_bm = null;
        for (int i=0;i<list.size();i++){
            Map<String,String> map2 = list.get(i);
            if(map2.get("path")==null){
                //*后缀名需小写（.toLowerCase()）
                Sprites_Bitmap.put(map2.get("name").toLowerCase(),Bitmap.createBitmap(all_bm,Integer.parseInt(map2.get("x")),Integer.parseInt(map2.get("y")),Integer.parseInt(map2.get("w")),Integer.parseInt(map2.get("h"))));
            }else {
                //根据图片路径获取精灵图包
                all_bm = FileUtil.read_Image(MainActivity.path+map2.get("path"));
            }
        }
        return Sprites_Bitmap;
    }

    public static void angle(float angle) {
        //计算角度
        angle = (float) (-angle/((90/(Math.PI/2))));
        Map<String,String> Ships_Parameter = Ships_List.get(PartList_ID);
        Ships_Parameter.put("angle",angle+"");
        //Log.e("angle",angle+"");
        Ships_List.set(PartList_ID,Ships_Parameter);
    }

    public static void saveimage(final RocketDesignActivity rocketDesignActivity) {
        new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] p1)
            {

                SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd-HHmmss");
                File file=new File(Environment.getExternalStorageDirectory(),sdf.format(System.currentTimeMillis())+".png");
                try
                {
                    String stage="Saving";
                    file.createNewFile();
                    FileOutputStream fos=new FileOutputStream(file);
                    if(bitmap.compress(Bitmap.CompressFormat.PNG,90,fos)){
                        fos.flush();
                        fos.close();
                        stage="Saved As"+"/DCIM/"+sdf.format(System.currentTimeMillis())+".png";
                        Uri uri = Uri.fromFile(file);
                        rocketDesignActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    }
                }catch (IOException e){

                }
                return null;
            }
        }.execute();
    }
}
