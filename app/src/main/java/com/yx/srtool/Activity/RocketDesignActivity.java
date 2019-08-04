package com.yx.srtool.Activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yx.srtool.R;
import com.yx.srtool.Utils.FileUtil;
import com.yx.srtool.View.MyViewPager;
import com.yx.srtool.View.RocketView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yx on 2019/4/22.
 */

public class RocketDesignActivity extends AppCompatActivity {
    TabLayout mTabLayout;
    MyViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design_rocket);
        //初始化标题栏
        init_toolbar();
        mTabLayout = (TabLayout) findViewById(R.id.rocket_tablayout);
        mViewPager = (MyViewPager) findViewById(R.id.rocket_pager);
        mViewPager.setScanScroll(false);

        initData();
        initView();
    }



    private static LinearLayout mHiddenLayout;
    private static SeekBar seekBar;
    private static TextView textview_type,textview_id;
    private static float mDensity;
    private void init_toolbar() {
        Toolbar toolbar= (Toolbar) findViewById(R.id.rocket_toolbar);//获取组件
        toolbar.setTitle("");//设置标题
        setSupportActionBar(toolbar);//替换原生toolbar
    }
    String[] Tab_Title = new String[2];
    private void initData() {
        Tab_Title[0] = "视图";//视图
        Tab_Title[1] = "代码";//编辑
    }
    private void initView() {
        //定义一个视图集合（用来装左右滑动的页面视图）
        final List<View> viewList = new ArrayList<View>();
        //定义几个视图，每个视图都加载同一个布局文件list_view.ml
        View view0 = getLayoutInflater().inflate(R.layout.activity_design_rocket_view,null);
        View view1 = getLayoutInflater().inflate(R.layout.activity_design_rocket_code,null);
        //将每个视图添加到视图集合viewList中
        viewList.add(view0);
        viewList.add(view1);
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
        //设置下滑框
        mHiddenLayout = (LinearLayout) view0.findViewById(R.id.linear_hidden);
        mDensity = getResources().getDisplayMetrics().density;
        animateOpen();

        final EditText edittext = (EditText) view0.findViewById(R.id.edittext_jiaodu);
        final com.yx.srtool.View.MyEditText EditText_Code = (com.yx.srtool.View.MyEditText) view1.findViewById(R.id.EditText_Code);
        textview_type  = (TextView) view0.findViewById(R.id.textview_type);
        textview_id  = (TextView) view0.findViewById(R.id.textview_id);
        CheckBox cb_1  = (CheckBox) view0.findViewById(R.id.cb_1);
        cb_1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if(isChecked){
                    RocketView.selectMOVE = true;
                    //Toast.makeText(RocketDesignActivity.this, "选中", Toast.LENGTH_SHORT).show();
                }else{
                    RocketView.selectMOVE = false;
                    //Toast.makeText(RocketDesignActivity.this, "取消选中", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seekBar = (SeekBar) view0.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //进度条发生改变时会触发
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if(shoudong2){
                    edittext.setText(progress+"");
                }
                shoudong2=true;
                //Toast.makeText(RocketDesignActivity.this, "当前进度条是" + progress + "/100", Toast.LENGTH_SHORT).show();
            }

            //按住seekbar时会触发
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(RocketDesignActivity.this, "按住seekbar时会触发", Toast.LENGTH_SHORT).show();
            }

            //放开seekbar时会触发
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(RocketDesignActivity.this, "放开seekbar时会触发", Toast.LENGTH_SHORT).show();
            }
        });
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                shoudong2=false;
                if(shoudong){
                    if(s.toString()!=null && s.toString()!="" && !s.toString().equals("")){
                        //设置当前选中组件旋转
                        RocketView.angle(Float.parseFloat(s.toString()));
                        //设置进度条位置
                        seekBar.setProgress(Integer.parseInt(s.toString()));
                    }else {
                        //设置当前选中组件旋转
                        RocketView.angle(0);
                        //设置进度条位置
                        seekBar.setProgress(0);
                    }
                }
                shoudong=true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //android.R.layout.simple_list_item_1是android自带的一个布局，只有一个textview
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //切换ViewPager
                mViewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        //保存代码
                        RocketView.ship = EditText_Code.getText().toString();
                        try {
                            RocketView.initxml();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        String data = RocketView.ship;
                        if(data.indexOf("><")!=-1){
                            EditText_Code.setText(data.replace(">",">\n"));
                        }else {
                            EditText_Code.setText(data);
                        }
                        //更新代码
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rocket, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_baochun:
                //保存
                FileUtil.write("ships/"+ MainActivity.ship_name,RocketView.ship);
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_p:
                //导出图片
                RocketView.saveimage(this);

                Toast.makeText(this, "图片已保存至sd卡根目录", Toast.LENGTH_SHORT).show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //对返回键进行监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //初始化view
            RocketView.PartList_ID=-1;
            RocketView.selectMOVE = false;
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private static void animateOpen() {
        if (mHiddenLayout.getVisibility() == View.GONE) {
            int mHiddenViewMeasuredHeight = (int) (mDensity * 140 + 0.5);
            ValueAnimator animator = createDropAnimator(mHiddenLayout, 0,
                    mHiddenViewMeasuredHeight);
            animator.start();
            mHiddenLayout.setVisibility(View.VISIBLE);
        }
    }
    private static void animateClose() {
        mHiddenLayout.setVisibility(View.GONE);
    }
    private static ValueAnimator createDropAnimator(final View v, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                int value = (int) arg0.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    private static boolean shoudong = true,shoudong2 = true;
    public static void click_focus(String type,String id,float angle) {
        //animateClose();
        animateOpen();
        textview_type.setText("类型:"+type);
        textview_id.setText("ID:"+id);
        seekBar.setProgress((int) angle);
        shoudong = false;
    }
    public static void click_notfocus() {
        animateClose();
    }
}