package com.hackathon.android;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/*
 * SMSMuster is an activity that allows you to play around with it's broadcastreciever.
 * 
 * The broadcast receiver responds to SMS messages and connects to the webservice with 
 * a url query containing the senders number and message.
 * 
 * The response from the web service is then relayed back to the original sender of 
 * the SMS, via SMS.
 * 
 */
public class SMSMuster extends Activity
{
    private static final int SMSMUSTER_NOTI_ID = 1;

    private Button sendTestTextButton;
    private EditText senderNumber;

    private OnClickListener sendTestTextButtonListener = new OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            SmsManager sms = SmsManager.getDefault();

            // I didn't want all my texts being automatically replied to so I
            // made it so there had to be a "." in front for it to reply...
            sms.sendTextMessage(senderNumber.getText().toString(), null,
                    "commands", null, null);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        senderNumber = (EditText) findViewById(R.id.sendTestNumber);

        sendTestTextButton = (Button) findViewById(R.id.sendTestText);
        sendTestTextButton.setOnClickListener(sendTestTextButtonListener);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.sms_icon;
        CharSequence tickerText = "SMSMuster running";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        Context context = getApplicationContext();
        CharSequence contentTitle = "SMSMuster";
        CharSequence contentText = "SMSMuster is ready to serve";
        Intent notificationIntent = new Intent(this, SMSMuster.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);

        mNotificationManager.notify(SMSMUSTER_NOTI_ID, notification);
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancelAll();
    }

}