package com.yx.srtool.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yx.srtool.Activity.MainActivity;
import com.yx.srtool.Utils.Util;
import com.yx.srtool.Utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GalaxyView extends View {
	private boolean Drag;
	private float eventX0=400,eventY0=600;//中心默认坐标
	private float[] eventAll;              //行星坐标，[0]为数量
	private long Multiple=100000000;       //放大参数
	private int time,time2;					//轨道变化时间,time是顺时针轨道，time2是逆时针轨道
	private Paint Paint_Line,Paint_Texe,Paint_graph;//画笔,Paint_Line线,Paint_Texe文字，Paint_graph图形
	private float[] fixed = new float[8];               // 顺时针记录绘制圆形的四个数据点
	private float[] mCtrl = new float[16];              // 顺时针记录绘制圆形的八个控制点
	private static final float C = 0.552284749831f;     // 用来计算绘制圆形贝塞尔曲线控制点的位置的常数
	private static String Message_code_new;				// 存储从Main传来的新的代码

	public static String galaxy;//星系数据
	private Map<Integer,Map<String,String>> Message_map =new HashMap<>();
	Handler handler_time = new Handler();                //时间线程
	int xingxing_jiaodian;
	String[] int_name;									//储存所有的行星名称

	private void initData() {
		//画线笔
		Paint_Line = new Paint();
		Paint_Line.setColor(Color.rgb(230,230,250));
		Paint_Line.setStrokeWidth(8);
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
		//执行时间线程
		handler_time.postDelayed(runnable,10);

		//获取载具文件
		File file = new File(MainActivity.path+"galaxy/"+ MainActivity.galaxy_name);
		try {
			galaxy = FileUtil.read2(file);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			//每10毫秒加一
			//ps:这是调试用的，到时候会写的复杂些
			time = time+1;
			time2 = time2+1;
			invalidate();
			handler_time.postDelayed(runnable,10);
		}
	};
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(eventX0,eventY0);//移动中心坐标
		canvas.drawColor(Color.rgb(0, 0, 0));//背景颜色
		//canvas.scale(1,1);// 翻转Y轴
		//获取从代码区传来的代码
		try {
			Message_map = Util.parseXMLWithPull(galaxy);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//初始化数量
		eventAll = new float[Message_map.size()*3];
		int_name = new String[Message_map.size()*2];
		eventAll[0]=1;

		for(Map.Entry<Integer, Map<String,String>> entry : Message_map.entrySet()) {
			Map<String,String> map2 = new HashMap<String,String>();
			map2.putAll(entry.getValue());
			switch (map2.get("Children")){
				case "0"://恒星
					Paint_graph.setColor(Color.rgb(Integer.parseInt(map2.get("Color_r")),Integer.parseInt(map2.get("Color_g")),Integer.parseInt(map2.get("Color_b"))));//中心天体颜色
					canvas.drawCircle(0, 0, Float.parseFloat(map2.get("radius"))/(Multiple), Paint_graph);//太阳颜色
					canvas.drawText(map2.get("name"), 0,0, Paint_Texe);//中心天体名称
					int_name[0] = map2.get("name");
					break;
				case "1"://行星
					DrawOrbit(
							canvas,Integer.parseInt(map2.get("prograde")),//运行方向
							Double.parseDouble(map2.get("w")),
							0,
							0,
							Float.parseFloat(map2.get("a"))/(Multiple),
							Double.parseDouble(map2.get("e")));
					revolution(canvas,
							0,
							Integer.parseInt(map2.get("i")),
							Integer.parseInt(map2.get("prograde")),
							map2.get("name"),
							Float.parseFloat(map2.get("radius"))/(Multiple),0,
							Color.rgb(Integer.parseInt(map2.get("Color_r")),
									Integer.parseInt(map2.get("Color_g")),
									Integer.parseInt(map2.get("Color_b"))),0,0);

					break;
				case "2"://卫星
					DrawOrbit(
							canvas,Integer.parseInt(map2.get("prograde")),//运行方向
							0,
							eventAll[Integer.parseInt(map2.get("i"))+1],
							-eventAll[Integer.parseInt(map2.get("i"))],
							Float.parseFloat(map2.get("a"))/(Multiple),
							Double.parseDouble(map2.get("e")));
					//canvas.drawText(map2.get("name")+"|"+map2.get("i")+eventAll[Integer.parseInt(map2.get("i"))]+"|"+eventAll[Integer.parseInt(map2.get("i"))+1],eventAll[Integer.parseInt(map2.get("i"))],eventAll[Integer.parseInt(map2.get("i"))+1], Paint_Texe);

					revolution(canvas,
							1,
							Integer.parseInt(map2.get("i")),
							Integer.parseInt(map2.get("prograde")),
							map2.get("name"),
							Float.parseFloat(map2.get("radius"))/(Multiple),0,
							Color.rgb(Integer.parseInt(map2.get("Color_r")),
									Integer.parseInt(map2.get("Color_g")),
									Integer.parseInt(map2.get("Color_b"))),0,0);

					//canvas.drawText("key:"+, -eventX0,-eventY0+(Message_map.size()+9)*20+80, Paint_Texe);
					break;
			}
		}
		//显示数据
		int quchu=0;
		for (int i=1;i<eventAll.length-10;i=i+2){
			try {

				if(int_name[i]!=null){
					canvas.drawText(int_name[i]+"x:"+eventAll[i]+"y:"+eventAll[i+1], -eventX0,-eventY0+i*20-quchu, Paint_Texe);
				}else{
					quchu = quchu+40;
				}

			}catch (Exception e) {
				quchu = quchu+40;
				e.printStackTrace();
			}
		}
		/*
		canvas.drawText("Multiple:"+Multiple, -eventX0,-eventY0+(Message_map.size()+5)*20, Paint_Texe);
		canvas.drawText("nLenStart3:"+nLenStart3, -eventX0,-eventY0+(Message_map.size()+6)*20+20, Paint_Texe);

		canvas.drawText("eventX:"+ceseventX, -eventX0,-eventY0+(Message_map.size()+7)*20+40, Paint_Texe);
		canvas.drawText("eventY:"+ceseventY, -eventX0,-eventY0+(Message_map.size()+8)*20+60, Paint_Texe);
		*/
		//canvas.drawText("eventY0:"+eventY0, -eventX0,-eventY0+(Message_map.size()+6)*20+100, Paint_Texe);

		//行星焦点
		if(xingxing_jiaodian !=0){
			if(eventAll[xingxing_jiaodian]>eventX2){
				eventX0=eventX0-(eventAll[xingxing_jiaodian]-eventX2);
			}else if(eventAll[xingxing_jiaodian]<eventX2){
				eventX0=eventX0+(eventX2-eventAll[xingxing_jiaodian]);
			}
			if (eventAll[xingxing_jiaodian+1]>eventY2) {
				eventY0=eventY0-(eventAll[xingxing_jiaodian+1]-eventY2);
			}else if (eventAll[xingxing_jiaodian+1]<eventY2) {
				eventY0=eventY0+(eventY2-eventAll[xingxing_jiaodian+1]);
			}
			eventX2 = eventAll[xingxing_jiaodian];
			eventY2 = eventAll[xingxing_jiaodian+1];
		}
	}

	/**
	 *轨道旋转计算公式
	 * @param θ 角度
	 * @param x 坐标X
	 * @param y 坐标y
	 * @return
	 */
	private double[] getRotatePoint(int θ,float x, float y,float x2, float y2) {
		double[] spot = new double[2];
		spot[0]= (x - 0)*Math.cos(Math.PI/180*θ) - (y - 0)*Math.sin(Math.PI/180*θ) + 0;
		spot[1]= (x - 0)*Math.sin(Math.PI/180*θ) + (y - 0)*Math.cos(Math.PI/180*θ) + 0;
		return spot;
	}
	int int0_2=0,int1_2=1,int2_2=2,int3_2=3;
	int int0=4,int1=5,int2=6,int3=7;
	/**
	 *
	 * @param canvas
	 * @param prograde 方向
	 * @param name
	 * @param r 半径
	 * @param gravity 重力
	 * @param Color 颜色
	 * @param v 始方位
	 * @param t 时间
	 */
	private void revolution(Canvas canvas,int type,int index,int prograde,String name,float r,double gravity,int Color,double v,int t) {
		float eventX2=0,eventY2=0;
		Paint_graph.setColor(Color);
		switch (prograde) {
			case 0:
				eventX2 = evaluate((float) ((float) time*0.0001),fixed[int2_2],mCtrl[int0_2*2+2],mCtrl[int0_2*2],fixed[int0_2]);//贝尔曲线求X坐标
				eventY2 = evaluate((float) ((float) time*0.0001),fixed[int3_2],mCtrl[int1_2*2+1],mCtrl[int1_2*2-1],fixed[int1_2]);//贝尔曲线求y坐标

				canvas.drawCircle(eventX2, eventY2,r, Paint_graph);
				canvas.drawText(name, eventX2-40,eventY2+10, Paint_Texe);
				//time++;
				if(time>10000) {
					time=0;
					int0_2=int0_2-2;
					int1_2=int1_2-2;
					int2_2=int2_2-2;
					int3_2=int3_2-2;
					if(int2_2==0) {
						int0_2=6;
						int1_2=7;
						int2_2=0;
						int3_2=1;
					}
					if(int2_2==-2) {
						int0_2=4;
						int1_2=5;
						int2_2=6;
						int3_2=7;
					}

				}
				break;
			case 1:
				eventX2 = evaluate((float) ((float) time2*0.0001),fixed[int0],mCtrl[int0*2],mCtrl[int0*2+2],fixed[int2]);
				eventY2 = evaluate((float) ((float) time2*0.0001),fixed[int1],mCtrl[int1*2-1],mCtrl[int1*2+1],fixed[int3]);
				canvas.drawCircle(eventX2, eventY2,r, Paint_graph);
				canvas.drawText(name, eventX2-40,eventY2+10, Paint_Texe);


				//time++;
				if(time2>10000) {
					time2=0;
					int0=int0+2;
					int1=int1+2;
					int2=int2+2;
					int3=int3+2;
					if(int2==8) {
						int2=0;
						int3=1;
					}
					if(int2==2) {
						int0=0;
						int1=1;
					}
				}
				break;
		}
		if(type<1){
			eventAll[index] = eventX2;
			eventAll[index+1] = eventY2;
			eventAll[0] = index+2;
			int_name[index] =name;
		}
	}



	/**
	 *
	 * @param canvas
	 * @param prograde 星球公转的方向
	 * @param w 黄经(0~6)
	 * @param coreX 坐标X
	 * @param coreY 坐标Y
	 * @param longaxis 半长轴
	 * @param eccentricity 偏心率
	 */
	private void DrawOrbit(Canvas canvas,int prograde,double w,float coreX,float coreY, float longaxis,double eccentricity) {
		float mDifference = longaxis * C;  //圆形的控制点与数据点的差值
		int θ=0;
		switch (prograde) {
			case 0:
				θ = (int) -(w*60-90);
				break;
			case 1:
				θ = (int) (w*60+90);
				break;
		}
		//求半焦距
		double focus = longaxis*eccentricity;//e=c/a,c=ae
		//求半短轴
		float shortaxis = (float) Math.sqrt(longaxis*longaxis-(focus)*(focus));
		//离心率等于零,也就意味c/a等于零,所以只有c=0,所以a=b,所以就是圆了,c就是焦点，a就是长轴
		if(focus!=0) {//不等于圆
			focus=(float) (longaxis-(longaxis-focus));
		}
		//上
		fixed[4] = coreX;
		fixed[5] = (float) (focus+coreY-longaxis);
		//下
		fixed[0] = coreX;
		fixed[1] = (float) (focus+coreY+longaxis);
		//右
		fixed[2] = coreX+shortaxis;
		fixed[3] = (float) (focus+coreY);
		//左
		fixed[6] = coreX-shortaxis;
		fixed[7] = (float) (focus+coreY);
		float xiuzhi=(longaxis-shortaxis)* C;
		mCtrl[0]  = fixed[0]+ mDifference-xiuzhi;
		mCtrl[1]  = fixed[1];

		mCtrl[2]  = fixed[2];
		mCtrl[3]  = fixed[3]+ mDifference+xiuzhi/6;

		mCtrl[4]  = fixed[2];
		mCtrl[5]  = fixed[3]- mDifference-xiuzhi/6;

		mCtrl[6]  = fixed[4]+ mDifference-xiuzhi;
		mCtrl[7]  = fixed[5];

		mCtrl[8]  = fixed[4]- mDifference+xiuzhi;
		mCtrl[9]  = fixed[5];

		mCtrl[10] = fixed[6];
		mCtrl[11] = fixed[7]- mDifference-xiuzhi/6;

		mCtrl[12] = fixed[6];
		mCtrl[13] = fixed[7]+ mDifference+xiuzhi/6;

		mCtrl[14] = fixed[0]- mDifference+xiuzhi;
		mCtrl[15] = fixed[1];

		//canvas.drawText(-coreY+"|"+coreX, -coreY+100,coreX+10, Paint_Texe);
		//轨道旋转
		float bei = fixed[0];
		fixed[0] = (float) getRotatePoint(θ,bei,fixed[1],-coreY,coreX)[0];
		fixed[1] = (float)getRotatePoint(θ,bei,fixed[1],-coreY,coreX)[1];
		bei = fixed[2];
		fixed[2] = (float)getRotatePoint(θ,bei,fixed[3],-coreY,coreX)[0];
		fixed[3] = (float)getRotatePoint(θ,bei,fixed[3],-coreY,coreX)[1];
		bei = fixed[4];
		fixed[4] = (float)getRotatePoint(θ,bei,fixed[5],-coreY,coreX)[0];
		fixed[5] = (float)getRotatePoint(θ,bei,fixed[5],-coreY,coreX)[1];
		bei = fixed[6];
		fixed[6] = (float)getRotatePoint(θ,bei,fixed[7],-coreY,coreX)[0];
		fixed[7] = (float)getRotatePoint(θ,bei,fixed[7],-coreY,coreX)[1];
		bei = mCtrl[0];
		mCtrl[0] = (float)getRotatePoint(θ,bei,mCtrl[1],-coreY,coreX)[0];
		mCtrl[1] = (float)getRotatePoint(θ,bei,mCtrl[1],-coreY,coreX)[1];
		bei = mCtrl[2];
		mCtrl[2] = (float)getRotatePoint(θ,bei,mCtrl[3],-coreY,coreX)[0];
		mCtrl[3] = (float)getRotatePoint(θ,bei,mCtrl[3],-coreY,coreX)[1];
		bei = mCtrl[4];
		mCtrl[4] = (float)getRotatePoint(θ,bei,mCtrl[5],-coreY,coreX)[0];
		mCtrl[5] = (float)getRotatePoint(θ,bei,mCtrl[5],-coreY,coreX)[1];
		bei = mCtrl[6];
		mCtrl[6] = (float)getRotatePoint(θ,bei,mCtrl[7],-coreY,coreX)[0];
		mCtrl[7] = (float)getRotatePoint(θ,bei,mCtrl[7],-coreY,coreX)[1];
		bei = mCtrl[8];
		mCtrl[8] = (float)getRotatePoint(θ,bei,mCtrl[9],-coreY,coreX)[0];
		mCtrl[9] = (float)getRotatePoint(θ,bei,mCtrl[9],-coreY,coreX)[1];
		bei = mCtrl[10];
		mCtrl[10] =(float) getRotatePoint(θ,bei,mCtrl[11],-coreY,coreX)[0];
		mCtrl[11] = (float)getRotatePoint(θ,bei,mCtrl[11],-coreY,coreX)[1];
		bei = mCtrl[12];
		mCtrl[12] = (float)getRotatePoint(θ,bei,mCtrl[13],-coreY,coreX)[0];
		mCtrl[13] = (float)getRotatePoint(θ,bei,mCtrl[13],-coreY,coreX)[1];
		bei = mCtrl[14];
		mCtrl[14] = (float)getRotatePoint(θ,bei,mCtrl[15],-coreY,coreX)[0];
		mCtrl[15] = (float)getRotatePoint(θ,bei,mCtrl[15],-coreY,coreX)[1];
		//辅助点
		/*
	    canvas.drawCircle(fixed[0], fixed[1], 5, Paint_graph);

	    canvas.drawCircle(mCtrl[0], mCtrl[1], 2, Paint_graph);
	    canvas.drawCircle(mCtrl[2], mCtrl[3], 2, Paint_graph);

	    canvas.drawCircle(fixed[2], fixed[3], 2, Paint_graph);

	    canvas.drawCircle(mCtrl[4], mCtrl[5], 2, Paint_graph);
	    canvas.drawCircle(mCtrl[6], mCtrl[7], 2, Paint_graph);

	    canvas.drawCircle(fixed[4], fixed[5], 2, Paint_graph);

	    canvas.drawCircle(mCtrl[8], mCtrl[9], 2, Paint_graph);
	    canvas.drawCircle(mCtrl[10], mCtrl[11], 2, Paint_graph);

	    canvas.drawCircle(fixed[6], fixed[7], 2, Paint_graph);

	    canvas.drawCircle(mCtrl[12], mCtrl[13], 2, Paint_graph);
	    canvas.drawCircle(mCtrl[14], mCtrl[15], 2, Paint_graph);
	    */

		// 绘制贝塞尔曲线
		Path path = new Path();
		path.moveTo(fixed[0],fixed[1]);
		path.cubicTo(mCtrl[0],mCtrl[1],mCtrl[2],mCtrl[3],fixed[2],fixed[3]);

		path.cubicTo(mCtrl[4],mCtrl[5],mCtrl[6],mCtrl[7],fixed[4],fixed[5]);

		path.cubicTo(mCtrl[8],mCtrl[9],mCtrl[10],mCtrl[11],fixed[6],fixed[7]);

		path.cubicTo(mCtrl[12],mCtrl[13],mCtrl[14],mCtrl[15],fixed[0],fixed[1]);

		canvas.drawPath(path, Paint_Line);
		canvas.drawPoint(fixed[0], fixed[1], Paint_Line);

	}
	/**
	 *
	 * @param fraction 变量
	 * @param point0 贝塞尔曲线起点
	 * @param point3 贝塞尔曲线终点
	 * @return 因为需要的点是从下到上....所以p0,p1,p2,p3的点是从下打上的
	 */
	public float evaluate(float fraction, float point0,float point1,float point2, float point3) {
		float currentPosition = point0 * (1 - fraction) * (1 - fraction) * (1 - fraction)
				+ point1 * 3 * fraction * (1 - fraction) * (1 - fraction)
				+ point2 * 3 * (1 - fraction) * fraction * fraction
				+ point3 * fraction * fraction * fraction;
		return currentPosition;
	}
	/**
	 * 屏幕事件
	 * 放大参考https://blog.csdn.net/csdnzouqi/article/details/79853109
	 */
	float eventX1,eventY1,eventX2,eventY2;
	private double nLenStart3;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int pCount = event.getPointerCount();// 触摸设备时手指的数量
		// 获取触屏动作。比如：按下、移动和抬起等手势动作
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN://0按下
				switch (pCount) {//手指数量
					case 1:
						float seventY=(eventY0-event.getY())*-1;
						float seventX=(eventX0-event.getX())*-1;

						//判断星球坐标被点击
						try {
							for (int i=1;i<eventAll.length;i++){
								if(eventAll[i] >= seventX-40 && eventAll[i] <= seventX+40 && eventAll[i+1] >= seventY-40 && eventAll[i+1] <= seventY+40){
									xingxing_jiaodian = i;
									break;
								}
							}
						}catch (Exception e) {
							e.printStackTrace();
						}


						eventX1 = event.getX();
						eventY1 = event.getY();

						Drag = true;
						break;
				}
				nLenStart3=0;
				break;
			case MotionEvent.ACTION_UP://1抬起
				Drag = false;
				nLenStart3=0;
				//抬起结束
				break;
			case MotionEvent.ACTION_MOVE://2移动
				//放大
				switch (pCount) {//手指数量
					case 1:

						if(Drag) {
							if(event.getX()>eventX1){
								eventX0=eventX0+(event.getX()-eventX1);
							}else if(event.getX()<eventX1){
								eventX0=eventX0-(eventX1-event.getX());
							}
							if (event.getY()>eventY1) {
								eventY0=eventY0+(event.getY()-eventY1);
							}else if (event.getY()<eventY1) {
								eventY0=eventY0-(eventY1-event.getY());
							}
							eventX1 = event.getX();
							eventY1 = event.getY();
						}

						break;
					case 2:
						Drag = false;
						// 获取按下时候两个坐标的x轴的水平距离，取绝对值
						int xLen = Math.abs((int)event.getX(0) - (int)event.getX(1));
						// 获取按下时候两个坐标的y轴的水平距离，取绝对值
						int yLen = Math.abs((int)event.getY(0) - (int)event.getY(1));
						// 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指刚按下时的直线距离
						int value = (int) Math.sqrt((double) xLen * xLen + (double) yLen * yLen);
						if(nLenStart3!=0){
							if(nLenStart3>value ) {
								//限制缩小
								/*
								if(Multiple<2000000000){
									Multiple = (long) (Multiple+Multiple/50);
								}
								*/
								Multiple = Multiple+Multiple/50;
							}else if(nLenStart3<value) {
								//限制放大
								if(Multiple>1000){
									Multiple = Multiple-Multiple/50;
								}
							}
							nLenStart3 = value;
						}else {
							nLenStart3 = value;
						}


						break;
				}
				break;
		}
		invalidate();
		return true;
	}

	public GalaxyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData();
	}
	public GalaxyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData();
	}
	public GalaxyView(Context context) {
		super(context);
		initData();
	}
}
