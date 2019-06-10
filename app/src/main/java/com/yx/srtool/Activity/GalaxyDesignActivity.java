package com.yx.srtool.Activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.yx.srtool.R;
import com.yx.srtool.View.GalaxyView;
import com.yx.srtool.View.MyViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yx on 2019/4/22.
 */

public class GalaxyDesignActivity extends AppCompatActivity {
    TabLayout mTabLayout;
    MyViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design_galaxy);
        //初始化标题栏
        init_toolbar();
        mTabLayout = (TabLayout) findViewById(R.id.galaxy_tablayout);
        mViewPager = (MyViewPager) findViewById(R.id.galaxy_pager);
        mViewPager.setScanScroll(false);

        initData();
        initView();
    }
    private void init_toolbar() {
        Toolbar toolbar= (Toolbar) findViewById(R.id.galaxy_toolbar);//获取组件
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
        View view0 = getLayoutInflater().inflate(R.layout.activity_design_galaxy_view,null);
        View view1 = getLayoutInflater().inflate(R.layout.activity_design_galaxy_code,null);
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
        final com.yx.srtool.View.MyEditText EditText_Code = (com.yx.srtool.View.MyEditText) view1.findViewById(R.id.EditText_Code);

        //android.R.layout.simple_list_item_1是android自带的一个布局，只有一个textview
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //切换ViewPager
                mViewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        //保存代码
                        GalaxyView.galaxy = EditText_Code.getText().toString();
                        break;
                    case 1:
                        String data = GalaxyView.galaxy;
                        if(data.indexOf("><")!=-1){
                            EditText_Code.setText(data.replace("><",">\n<"));
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
                //FileUtil.write("ships/"+ MainActivity.ship_name,RocketView.ship);
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //对返回键进行监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}