package com.hackathon.android;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class SMSService extends Service
{
    static final String ACTION_FOREGROUND = "com.hackathon.android.FOREGROUND";
    static final String ACTION_BACKGROUND = "com.hackathon.android.BACKGROUND";

    private static final String TAG = "SMSService";

    private static final Class[] mStartForegroundSignature = new Class[]
    { int.class, Notification.class };
    private static final Class[] mStopForegroundSignature = new Class[]
    { boolean.class };

    private static final int NOTIFICATION_ID = 15;
    
    public static boolean isRunning = false;

    private NotificationManager mNM;
    private SMSReceiver smsReceiver;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification)
    {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null)
        {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try
            {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e)
            {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e)
            {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }

        // Fall back on the old API.
        setForeground(true);
        mNM.notify(id, notification);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id)
    {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null)
        {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try
            {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e)
            {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e)
            {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }

        // Fall back on the old API. Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        setForeground(false);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try
        {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
        } catch (NoSuchMethodException e)
        {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        smsReceiver = new SMSReceiver();
        registerReceiver(smsReceiver, new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED"));
        Log.d(TAG, "service created");
    }

    @Override
    public void onDestroy()
    {
        
        // unregister the receiver
        if (smsReceiver != null)
        {
            unregisterReceiver(smsReceiver);
        }
        // Make sure our notification is gone.
        stopForegroundCompat(NOTIFICATION_ID);
        isRunning = false;
    }

    // This is the old onStart method that will be called on the pre-2.0
    // platform. On 2.0 or later we override onStartCommand() so this
    // method will not be called.
    @Override
    public void onStart(Intent intent, int startId)
    {
        handleCommand(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    void handleCommand(Intent intent)
    {
        if (ACTION_FOREGROUND.equals(intent.getAction()))
        {
            // In this sample, we'll use the same text for the ticker and the
            // expanded notification
            CharSequence text = "SMS Muster Started";

            // Set the icon, scrolling text and timestamp
            Notification notification = new Notification(R.drawable.sms_icon,
                    text, System.currentTimeMillis());

            // The PendingIntent to launch our activity if the user selects this
            // notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, SMSMuster.class), 0);

            // Set the info for the views that show in the notification panel.
            notification.setLatestEventInfo(this, "SMS Mustero", text,
                    contentIntent);

            startForegroundCompat(NOTIFICATION_ID, notification);
            isRunning = true;

        } else if (ACTION_BACKGROUND.equals(intent.getAction()))
        {
            stopForegroundCompat(NOTIFICATION_ID);
            
        }
    }

}
