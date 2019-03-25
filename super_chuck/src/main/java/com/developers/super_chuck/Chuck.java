package com.developers.super_chuck;

import android.content.Context;
import android.content.Intent;

import com.developers.super_chuck.internal.view.MainActivity;

/**
 * @Author yinzh
 * @Date 2019/3/25 15:14
 * @Description
 */
public class Chuck {

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
