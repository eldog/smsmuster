package com.hackathon.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * SMSReciever will listen to any SMS messages sent to the android device whilst
 * the application is running, and then send a HTTP GET request to the
 * SERVICE_URL with the paramaters "phonenumber" and "message" in the query
 * string. Phone number will be that of the person who sent the SMS to the phone
 * and the message will be the message that they sent.
 * 
 * 
 */
public class SMSReceiver extends BroadcastReceiver
{

    private static final String TAG = "smsreceiver";
    private static final String URL_TEMPLATE = "http://scraperwikiviews.com/run/mustersms?phonenumber=%s&message=%s";
    private static final String ENCODING = "utf-8";
    private Context currentContext;

    // TODO:Make it work for multipage texts... i.e. associate messages with
    // the same number...
    // Also can't handle multiple texts, needs to be able to build up a buffer
    // and stuff.
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // ---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        currentContext = context;
        sendLogMessage("recieved message!");

        if (bundle != null)
        {
            new SendMuster().execute(bundle);
        }
    }

    private void sendLogMessage(String message)
    {
        Intent messageIntent = new Intent(SMSMuster.SMSLOG);
        messageIntent.putExtra("message", message);
        if (currentContext != null)
        {
            Log.d(TAG, message);
            currentContext.sendBroadcast(messageIntent);
        } else
        {
            Log.e(TAG, "unable to senf log message, current context was null");
            
        }
    }

    private class SendMuster extends AsyncTask<Bundle, Void, Void>
    {

        @Override
        protected Void doInBackground(Bundle... bundle)
        {
            // retrieve the messages from the bundle
            SmsMessage[] msgs = null;
            Object[] pdus = (Object[]) bundle[0].get("pdus");
            msgs = new SmsMessage[pdus.length];

            // From each message in the bundle, query the web service with the
            // message and then send back the result via sms
            for (int i = 0; i < msgs.length; i++)
            {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String messageBody = msgs[i].getMessageBody().toString();
                String number = msgs[i].getOriginatingAddress();
                try
                {
                    sendLogMessage("sent from: " + number);
                    sendLogMessage("message: " + messageBody);
                    String response = postToService(number, messageBody);
                    if (response != null)
                    {
                        sendLogMessage("response = " + response);
                        try
                        {
                            sendLogMessage("parsing response");
                            if (response.length() > 0)
                            {

                                for (String line : response.split("#"))
                                {
                                    sendSMS(line
                                            .substring(0, line.indexOf(" ")),
                                            line.substring(line.indexOf(" ")));
                                }
                            } else
                            {
                                sendLogMessage("ERROR response was of zero length");
                            }
                        } catch (Exception e)
                        {
                            sendLogMessage("ERROR Problem in reading the response");
                            e.printStackTrace();
                        }
                    } else
                    {
                        sendLogMessage("ERROR response was null");
                    }
                } catch (IOException e1)
                {
                    sendLogMessage("ERROR Problem in IO posting to server.");
                    e1.printStackTrace();
                }
            }
            return null;
        }

        private String postToService(String number, String message)
                throws IOException
        {
            sendLogMessage("posting request");
            URL request = new URL(makeURLString(number, message));
            Log.d(TAG, request.toString());
            URLConnection rconnec = request.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    rconnec.getInputStream()));

            String input;
            StringBuffer sb = new StringBuffer();
            while ((input = in.readLine()) != null)
            {
                sb.append(input);
            }
            return sb.toString();
        }

        private String makeURLString(String phoneNumber, String message)
                throws UnsupportedEncodingException
        {
            phoneNumber = URLEncoder.encode(phoneNumber, ENCODING);
            message = URLEncoder.encode(message, ENCODING);

            return String.format(URL_TEMPLATE, phoneNumber, message);
        }

        // ---sends an SMS message to another device---
        private void sendSMS(String phoneNumber, String message)
        {
            sendLogMessage("Sending SMS to: " + phoneNumber);
            sendLogMessage("message: " + message);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            sendLogMessage("Message Sent");
        }
    }

}