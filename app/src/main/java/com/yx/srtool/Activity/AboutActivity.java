package com.yx.srtool.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yx.srtool.R;

/**
 * Created by Yx on 2019/4/22.
 */

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("关于");
    }
}
