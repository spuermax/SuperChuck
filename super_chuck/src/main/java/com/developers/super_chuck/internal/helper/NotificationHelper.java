package com.developers.super_chuck.internal.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.LongSparseArray;

import com.developers.super_chuck.Chuck;
import com.developers.super_chuck.R;
import com.developers.super_chuck.internal.data.HttpTransaction;
import com.developers.super_chuck.internal.view.BaseChuckActivity;

/**
 * @Author yinzh
 * @Date 2019/3/25 14:46
 * @Description
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "chuck";
    private static final String CHANNEL_NAME = "super_chuck";
    private static final int NOTIFICATION_ID = 1138;
    private static final int BUFFER_SIZE = 10;

    private static final LongSparseArray<HttpTransaction> transactionBuffer = new LongSparseArray<>();
    private static int transactionCount;

    private final Context context;
    private final NotificationManager notificationManager;

    public static synchronized void clearBuffer() {
        transactionBuffer.clear();
        transactionCount = 0;
    }

    private static synchronized void addToBuffer(HttpTransaction transaction) {
        if (transaction.getStatus() == HttpTransaction.Status.Requested) {
            transactionCount++;
        }
        transactionBuffer.put(transaction.getId(), transaction);
        if (transactionBuffer.size() > BUFFER_SIZE) {
            transactionBuffer.removeAt(0);
        }
    }


    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public synchronized void show(HttpTransaction transaction) {
        addToBuffer(transaction);
        if (!BaseChuckActivity.isInForeground()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentIntent(PendingIntent.getActivity(context, 0, Chuck.getLaunchIntent(context), 0))
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.chuck_ic_notification_white_24dp)
                    .setColor(ContextCompat.getColor(context, R.color.chuck_colorPrimary))
                    .setContentTitle(context.getString(R.string.chuck_notification_title));
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            int count = 0;
            for (int i = transactionBuffer.size() - 1; i >= 0; i--) {
                if (count < BUFFER_SIZE) {
                    if (count == 0) {
                        builder.setContentText(transactionBuffer.valueAt(i).getNotificationText());
                    }
                    inboxStyle.addLine(transactionBuffer.valueAt(i).getNotificationText());
                }
                count++;
            }

            builder.setAutoCancel(true);
            builder.setStyle(inboxStyle);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setSubText(String.valueOf(transactionCount));
            } else {
                builder.setNumber(transactionCount);
            }
            builder.addAction(getClearAction());
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    @NonNull
    private NotificationCompat.Action getClearAction() {
        CharSequence clearTitle = context.getString(R.string.chuck_clear);
        Intent deleteIntent = new Intent(context, ClearTransactionsService.class);
        PendingIntent intent = PendingIntent.getService(context, 11, deleteIntent, PendingIntent.FLAG_ONE_SHOT);
        return new NotificationCompat.Action(R.drawable.chuck_ic_delete_white_24dp,
                clearTitle, intent);
    }


    public void dismiss() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
