package com.yx.srtool.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyEditText extends android.support.v7.widget.AppCompatEditText
{
    public interface OnTextChangedListener {
        public void onTextChanged(String text);
    }
    private Paint Paint_Line,Paint_Texe,Paint_graph;//画笔,Paint_Line线,Paint_Texe文字，Paint_graph图形
    //在文本更改的侦听器上
    public OnTextChangedListener onTextChangedListener = null;
    public int updateDelay = 200;//时间1秒
    public boolean dirty = false;
    private boolean modified = true;

    private final Handler updateHandler = new Handler();
    //执行线程
    private final Runnable updateRunnable =
            new Runnable() {
                @Override
                public void run() {
                    Editable editable = getText();
                    //在文本更改的侦听器上
                    if (onTextChangedListener != null)
                        onTextChangedListener.onTextChanged(editable.toString());
                    highlightWithoutChange(editable);
                }
            };

    public MyEditText(Context context) {
        super(context);
        init();
    }
    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        //设置水平滚动
        setHorizontallyScrolling(true);
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
        //布局
        setPadding(100,0,0,0);
        setGravity(Gravity.TOP);

        //添加文本更改的侦听器
        addTextChangedListener(
                //文本监听
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                        /**
                         * start  输入位置
                         * count 文本长度
                         */
                        //更改前文本
                        //Log.e("文本监听-更改前文本","start"+start+",count"+count+",after"+after+"\n\n"+s.toString());
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        /**
                         * start  输入位置
                         * before  之前文本长度
                         * count 文本长度
                         */
                        //文本更改
                        //Log.i("TAG",s.length()+"");
                        //Log.e("文本监听-文本更改","start"+start+",before"+before+",count"+count+"\n\n"+s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable e) {//文本更改后
                        //Log.e("文本监听-文本更改后",e.toString());
                        //取消刷新高亮
                        cancelUpdate();

                        if (!modified)
                            return;

                        dirty = true;
                        updateHandler.postDelayed(
                                updateRunnable,//执行线程
                                updateDelay);//时间
                    }
                }
        );
    }
    /**
     * 取消刷新高亮
     */
    private void cancelUpdate() {
        updateHandler.removeCallbacks(updateRunnable);
    }
    /**
     * 突出显示而不更改
     * @param editable
     */
    private void highlightWithoutChange(Editable editable) {
        modified = false;
        //高亮
        highlight(editable);
        modified = true;
    }
    /**
     * 执行高亮替换
     * @param editable
     * @return
     */
    private Editable highlight(Editable editable) {
        if (editable.length() == 0)
            return editable;
        Log.e("注释颜色","颜色开始更改");
        //恢复默认
        editable.setSpan(
                new ForegroundColorSpan(0xFF000000),
                0,
                editable.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //<开头颜色
        Pattern comments = Pattern.compile("<[0-9a-zA-Z/]*\\b");
        for (Matcher m = comments.matcher(editable);
             m.find(); )
            editable.setSpan(
                    new ForegroundColorSpan(0xFF020281),
                    m.start(),
                    m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        comments = Pattern.compile("(|/)>");
        for (Matcher m = comments.matcher(editable);
             m.find(); )
            editable.setSpan(
                    new ForegroundColorSpan(0xFF020281),
                    m.start(),
                    m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //项的颜色
        comments = Pattern.compile("\\b[\u4E00-\u9FA50-9a-zA-Z]*[ ]*=");
        for (Matcher m = comments.matcher(editable);
             m.find(); )
            editable.setSpan(
                    new ForegroundColorSpan(0xFF0404FF),
                    m.start(),
                    m.end()-1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //值的颜色
        comments = Pattern.compile("=[ ]*\".[^\"]*\"");
        for (Matcher m = comments.matcher(editable);
             m.find(); )
            editable.setSpan(
                    new ForegroundColorSpan(0xFF028102),
                    m.start()+1,
                    m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //注释颜色
        comments = Pattern.compile("<!--([\\s\\S]*?)-->");
        for (Matcher m = comments.matcher(editable);
             m.find(); )
            editable.setSpan(
                    new ForegroundColorSpan(0xff808080),
                    m.start(),
                    m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Log.e("注释颜色","颜色更改完成");
        return editable;
    }
    @Override
    protected void onDraw(final Canvas canvas)
    {
        int k=getLineHeight();
        int i=getLineCount();
        //canvas.drawLine(90,0,90,getHeight()+(i*k),);
        //r1.right = 25*(getLineCount()+"").length();
        RectF r1 = new RectF();
        r1.left = 0;
        r1.right = 90;
        r1.top = 0 ;
        r1.bottom = getHeight()+(i*k);
        Paint_graph.setColor(0xFFF0F0F0);
        canvas.drawRoundRect(r1 , 0, 0, Paint_graph);

        int y2=(getLayout().getLineForOffset(getSelectionStart())+1)*k;
        //canvas.drawLine(0,y2,getWidth(),y2,line);
        RectF r2 = new RectF();
        r2.left = 100;
        r2.right = getWidth();
        r2.top = y2-50 ;
        r2.bottom = y2+5;

        Paint_graph.setColor(0xFFFFFAE3);
        canvas.drawRoundRect(r2 , 0, 0, Paint_graph);
        canvas.save();
        canvas.restore();

        if(getText().toString().length()!=0){
            float y=0;
            Paint p=new Paint();
            p.setColor(Color.GRAY);
            p.setAntiAlias(true);
            for(int l=0;l<getLineCount();l++){
                p.setTextSize(50-(l+"").length()*5);
                y=((l+1)*getLineHeight())-(getLineHeight()/4);
                canvas.drawText(String.valueOf(l+1),10,y,p);
                canvas.save();
            }
        }
        super.onDraw(canvas);
        invalidate();
    }
}

