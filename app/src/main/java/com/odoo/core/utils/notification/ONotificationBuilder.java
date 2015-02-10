/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 9/1/15 6:51 PM
 */
package com.odoo.core.utils.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.odoo.core.account.BaseSettings;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.OResource;
import com.odoo.crm.R;

import java.util.ArrayList;
import java.util.List;


public class ONotificationBuilder {
    public static final String TAG = ONotificationBuilder.class.getSimpleName();
    private Context mContext;
    private Builder mNotificationBuilder = null;
    private PendingIntent mNotificationResultIntent = null;
    private NotificationManager mNotificationManager = null;
    private String title, text, bigText;
    private boolean mOnGoing = false, mAutoCancel = true;
    private Intent resultIntent = null;
    private int icon = R.drawable.ic_odoo_o_white;
    private int small_icon = R.drawable.ic_odoo_o_white;
    private List<NotificationAction> mActions = new ArrayList<ONotificationBuilder.NotificationAction>();
    private int notification_id = 0;
    private Boolean withVibrate = true;
    private OPreferenceManager mPref;

    public ONotificationBuilder(Context context, int notification_id) {
        mContext = context;
        this.notification_id = notification_id;
        mPref = new OPreferenceManager(context);
    }

    public ONotificationBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public ONotificationBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public ONotificationBuilder setIcon(int res_id) {
        icon = res_id;
        return this;
    }

    public ONotificationBuilder setBigText(String bigText) {
        this.bigText = bigText;
        return this;
    }

    public ONotificationBuilder setOngoing(boolean onGoing) {
        mOnGoing = onGoing;
        return this;
    }

    public ONotificationBuilder setAutoCancel(boolean autoCancel) {
        mAutoCancel = autoCancel;
        return this;
    }

    public ONotificationBuilder addAction(NotificationAction action) {
        mActions.add(action);
        return this;
    }

    public ONotificationBuilder allowVibrate(Boolean vibrate) {
        withVibrate = vibrate;
        return this;
    }

    private void init() {
        mNotificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(mContext);
        mNotificationBuilder.setContentTitle(title);
        mNotificationBuilder.setContentText(text);
        mNotificationBuilder.setContentInfo(text);
        mNotificationBuilder.setSmallIcon(small_icon);
        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), this.icon);
        Bitmap newIcon = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), icon.getConfig());
        Canvas canvas = new Canvas(newIcon);
        canvas.drawColor(OResource.color(mContext, R.color.theme_primary));
        canvas.drawBitmap(icon, 0, 0, null);
        mNotificationBuilder.setLargeIcon(newIcon);
        mNotificationBuilder.setAutoCancel(mAutoCancel);
        mNotificationBuilder.setOngoing(mOnGoing);
        mNotificationBuilder.setColor(OResource.color(mContext, R.color.theme_secondary));
        if (bigText != null) {
            NotificationCompat.BigTextStyle notiStyle = new NotificationCompat.BigTextStyle();
            notiStyle.setBigContentTitle(title);
            notiStyle.setSummaryText(text);
            notiStyle.bigText(bigText);
            mNotificationBuilder.setStyle(notiStyle);
        }
    }

    private void setSoundForNotification() {
        Uri uri = BaseSettings.getNotificationRingTone(mContext);
        mNotificationBuilder.setSound(uri);
    }

    private void setVibrateForNotification() {
        mNotificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000,
                1000});
    }

    public ONotificationBuilder setResultIntent(Intent intent) {
        resultIntent = intent;
        return this;
    }

    public ONotificationBuilder build() {
        init();
        if (withVibrate) {
            setVibrateForNotification();
        }
        setSoundForNotification();
        if (resultIntent != null) {
            _setResultIntent();
        }
        if (mActions.size() > 0) {
            _addActions();
        }
        return this;
    }

    private void _addActions() {
        for (NotificationAction action : mActions) {
            Intent intent = new Intent(mContext, action.getIntent());
            intent.setAction(action.getAction());
            intent.putExtras(action.getExtras());
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.addAction(action.getIcon(),
                    action.getTitle(), pendingIntent);
        }
    }

    private void _setResultIntent() {
        mNotificationResultIntent = PendingIntent.getActivity(mContext, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        mNotificationBuilder.setContentIntent(mNotificationResultIntent);
    }

    public void show() {
        mNotificationManager.notify(notification_id, mNotificationBuilder.build());
    }

    public static void cancelNotification(Context context, int id) {
        NotificationManager nMgr = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        nMgr.cancel(id);
    }


    public static class NotificationAction {
        private int icon;
        private int requestCode;
        private String title;
        private String action;
        private Bundle extras;
        private Class<?> intent;

        public NotificationAction(int icon, String title, int requestCode,
                                  String action, Class<?> intent, Bundle extras) {
            super();
            this.icon = icon;
            this.title = title;
            this.requestCode = requestCode;
            this.action = action;
            this.intent = intent;
            this.extras = extras;
        }

        public Class<?> getIntent() {
            return intent;
        }

        public void setIntent(Class<?> intent) {
            this.intent = intent;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public int getRequestCode() {
            return requestCode;
        }

        public void setRequestCode(int requestCode) {
            this.requestCode = requestCode;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Bundle getExtras() {
            return extras;
        }

        public void setExtras(Bundle extras) {
            this.extras = extras;
        }

    }
}
