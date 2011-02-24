package com.hackathon.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

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
    private static final String SMSSERVICE = "com.hackathon.android.FOREGROUND";
    static final String ACTION_BACKGROUND = "com.hackathon.android.BACKGROUND";
    
    public static final String SMSLOG = "com.hackathon.android.SMSLOG";
    
    private static final String TAG = "smsmuster";
    
    private ArrayList<String> logArray = new ArrayList<String>();
    private ArrayAdapter<String> logAdapter;
    

    private Button startServerButton;
    private ListView logTable; 

    private OnClickListener startServerButtonListener = new OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            
            

            if (!SMSService.isRunning)
            {
                // if it's not running start the service
                Intent serviceIntent = new Intent(SMSSERVICE);
                serviceIntent.setClass(SMSMuster.this, SMSService.class);
                startService(serviceIntent);
                startServerButton.setText(getText(R.string.stop_server_text));
            } else
            {
                // else stop it
                Intent serviceIntent = new Intent(ACTION_BACKGROUND);
                serviceIntent.setClass(SMSMuster.this, SMSService.class);
                stopService(serviceIntent);
                startServerButton.setText(getText(R.string.send_test_text));
            }

        }
    };
    
    private boolean isServiceRunning()
    {

        boolean serviceStarted = false;
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningServices= am.getRunningServices(50);
        
        for (RunningServiceInfo service : runningServices)
        {
            Log.d(TAG, service.service.getClassName());
            if (service.service.getClass().equals(SMSService.class))
            {
                Log.d(TAG, service.service.getClassName());
                serviceStarted = true;
                break;
            }
        }
        
        return serviceStarted;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        startServerButton = (Button) findViewById(R.id.sendTestText);
        startServerButton.setOnClickListener(startServerButtonListener);
        
        logTable = (ListView) findViewById(R.id.logTable);
        logAdapter = new ArrayAdapter<String>(this, R.layout.log_list_item, logArray);
        logTable.setAdapter(logAdapter);
        registerReceiver(new LogReceiver(), new IntentFilter(SMSLOG));
        if (SMSService.isRunning)
        {
            startServerButton.setText(getText(R.string.stop_server_text));
        }

    }

    public class LogReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, "recieved a log message");
            Bundle bundle = intent.getExtras();
            String message = (String) bundle.get("message");
            
            //TextView rowText = new TextView(SMSMuster.this);
            //rowText.setText(message);
            logArray.add(0, message);
            logTable.setAdapter(logAdapter);
            
        }
        
    }

}