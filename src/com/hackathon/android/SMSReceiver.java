package com.hackathon.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver
{

    private static final String TAG = "smsreceiver";
    private static final String SERVICE_URL = "http://scraperwikiviews.com/run/mustersms";

    // Temporary URL used for testing query strings...
    private static final String TEST_URL = "http://seagrass.goatchurch.org.uk/~julian/cgi-bin/uc.cgi";

    // TODO:Make it work for multi-page texts... i.e. associate messages with
    // the same number...
    // Also can't handle multiple texts, needs to be able to build up a buffer
    // and stuff.
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // ---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;

        if (bundle != null)
        {
            // ---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++)
            {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                //if (!msgs[i].getMessageBody().toString().startsWith("."))
                //{
                //    continue;
                //}
                Log.d(TAG, "original Message "
                        + msgs[i].getMessageBody().toString());
                String messageBody = msgs[i].getMessageBody().toString().replace(" ", "+");
                String number = msgs[i].getOriginatingAddress().replace("+",
                        "a");
                Toast.makeText(context,
                        "Text recieved from: " + number + ":\n" + messageBody,
                        Toast.LENGTH_SHORT).show();
                try
                {
                    Log.d(TAG, "sending to: " + number);
                    Log.d(TAG, "message : " + messageBody);
                    String response = postToService(number, messageBody);
                    if (response != null)
                    {
                        Log.d(TAG, "response = " + response);
                        try
                        {
                            Log.d(TAG, "reading response");
                            for (String line : response.split("#"))
                            {
                                sendSMS(line.substring(0, line.indexOf(" ")),
                                        line.substring(line.indexOf(" ")));
                            }
                        } catch (Exception e)
                        {
                            // TODO Auto-generated catch block
                            Log.e(TAG, "Problem in reading the response");
                            e.printStackTrace();
                        }
                        Log.d(TAG, "Done reading response");
                    } else
                    {
                        Toast.makeText(context,
                                "Response is null, didn't work...",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e1)
                {
                    // TODO Auto-generated catch block
                    Log.e(TAG, "Problem in posting to server.");
                    e1.printStackTrace();
                }
            }
        }
    }

    private String postToService(String number, String message)
            throws IOException
    {
        Log.d(TAG, "posting request");

        String requestString = SERVICE_URL + "?phonenumber=" + number
                + "&message=" + message;
        requestString = requestString.trim();

        URL request = new URL(requestString);
        Log.d(TAG, request.toString());
        URLConnection rconnec = request.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(rconnec
                .getInputStream()));

        String input;
        StringBuffer sb = new StringBuffer();
        while ((input = in.readLine()) != null)
        {
            sb.append(input);
        }
        return sb.toString();
    }

    // ---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {
        Log.d(TAG, "Sending SMS");
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

}