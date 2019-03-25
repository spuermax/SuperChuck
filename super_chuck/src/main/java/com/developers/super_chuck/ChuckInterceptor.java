package com.developers.super_chuck;

import android.content.Context;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @Author yinzh
 * @Date 2019/3/25 14:22
 * @Description
 */
public class ChuckInterceptor implements Interceptor{

    public enum Period {
        /**
         * Retain data for the last hour.
         */
        ONE_HOUR,
        /**
         * Retain data for the last day.
         */
        ONE_DAY,
        /**
         * Retain data for the last week.
         */
        ONE_WEEK,
        /**
         * Retain data forever.
         */
        FOREVER
    }

    private static final String LOG_TAG= "ChuckInterceptor";
    private static final Period DEFAULT_RETENTION = Period.ONE_WEEK;
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final Context context;
    private final NotificationHelper notificationHelper;
    private RetentionManager retentionManager;
    private boolean showNotification;
    private long maxContentLength = 250000L;

    /**
     * @param context The current Context.
     */
    public ChuckInterceptor(Context context) {
        this.context = context.getApplicationContext();
        notificationHelper = new NotificationHelper(this.context);
        showNotification = true;
        retentionManager = new RetentionManager(this.context, DEFAULT_RETENTION);
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        return null;
    }
}
