package com.yx.srtool.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class AboutView extends View
{
	private Paint Paint_Line//Paint_Line线
			,Paint_Texe//Paint_Texe文字
			,Paint_graph;//Paint_graph图形

	Handler handler_time = new Handler();

	String[] content={"SimpleRocketsTool","Staff","程序,UI","雨夏","调试,后期","柳树怪","BGM","Beyond - Tridust","Thanks for using"};

	int time = 0;
	private void init() {
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
		handler_time.postDelayed(runnable,1000);
	}
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			//每10毫秒加一
			//ps:这是调试用的，到时候会写的复杂些
			time = time+1;
			handler_time.postDelayed(runnable,1000);
		}
	};
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawText(content[0], getWidth() / 2, getHeight() / 2, Paint_Texe);
		canvas.drawText(content[1], getWidth() / 2, getHeight() / 2+40*1, Paint_Texe);
		canvas.drawText(content[2], getWidth() / 2, getHeight() / 2+40*2, Paint_Texe);
		canvas.drawText(content[3], getWidth() / 2, getHeight() / 2+40*3, Paint_Texe);
		canvas.drawText(content[4], getWidth() / 2, getHeight() / 2+40*4, Paint_Texe);
		canvas.drawText(content[5], getWidth() / 2, getHeight() / 2+40*5, Paint_Texe);
		if (time > 3)
		{
			canvas.drawText(content[0], getWidth() / 2, getHeight() / 2, Paint_Texe);
		}
		if (time == 6)
		{
			canvas.drawText(content[1], getWidth() / 2, getHeight() / 2, Paint_Texe);
		}
		if (time == 10)
		{
			canvas.drawText(content[1], getWidth() / 2, getHeight() / 2, Paint_Texe);
		}

		super.onDraw(canvas);
	}
	public AboutView(Context context) {
		super(context);
		init();
	}
	public AboutView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AboutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
}
