package com.developers.super_chuck.internal.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.developers.super_chuck.internal.helper.NotificationHelper;

/**
 * @Author yinzh
 * @Date 2019/3/25 15:11
 * @Description
 */
public class BaseChuckActivity extends AppCompatActivity{
    private static boolean inForeground;

    private NotificationHelper notificationHelper;

    public static boolean isInForeground() {
        return inForeground;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationHelper = new NotificationHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inForeground = true;
        notificationHelper.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        inForeground = false;
    }

}
