package com.developers.super_chuck.internal.helper;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.developers.super_chuck.internal.data.ChuckContentProvider;

/**
 * @Author yinzh
 * @Date 2019/3/25 15:18
 * @Description
 */
public class ClearTransactionsService extends IntentService {

    public ClearTransactionsService() {
        super("Chuck-ClearTransactionsService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        getContentResolver().delete(ChuckContentProvider.TRANSACTION_URI, null, null);
        NotificationHelper.clearBuffer();
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.dismiss();
    }
}