package com.yx.srtool.Adapter;

/**
 * Created by 雨夏 on 2015/5/4.
 */
public class Bean {
    private String M_title,M_id;
    public Bean(String title, String id) {
        this.M_title = title;
        this.M_id = id;
    }
    public String gettitle() {
        return M_title;
    }
    public String getid() {
        return M_id;
    }
}