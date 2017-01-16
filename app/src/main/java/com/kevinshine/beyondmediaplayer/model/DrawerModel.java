package com.kevinshine.beyondmediaplayer.model;

import android.content.Context;

import com.kevinshine.beyondmediaplayer.model.bean.DrawerItemBean;

import java.util.ArrayList;

/**
 * Created by gary on 16-2-17.
 */
public class DrawerModel {
    private DrawerItemDao mDrawerItemDao;

    public DrawerModel(Context context){
        mDrawerItemDao = new DrawerItemDao(context);

    }
    public ArrayList<DrawerItemBean> getDrawerItems(String type){
        return mDrawerItemDao.getItems(type);
    }

    public void addDrawerItem(DrawerItemBean bean){
        mDrawerItemDao.addDrawerItem(bean);
    }
}
