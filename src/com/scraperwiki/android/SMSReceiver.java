package com.scraperwiki.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver
{

    private static final String TAG = "smsreceiver";
    private static final String SERVICE_URL = "http://www.scraperwiki.com/run/mustersms";

    // TODO:Make it work for multi-page texts... i.e. associate messages with
    // the same number...
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
                String str = "";
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                str += "SMS from " + msgs[i].getOriginatingAddress();
                str += " :";
                str += msgs[i].getMessageBody().toString();
                str += "\n";
                HttpResponse response = postToService(msgs[i]
                        .getOriginatingAddress(), msgs[i].getMessageBody()
                        .toString());
                Toast.makeText(context,
                        str + response.getStatusLine().getReasonPhrase(),
                        Toast.LENGTH_SHORT).show();
            }
            // ---display the new SMS message---

        }
    }

    private HttpResponse postToService(String number, String message)
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(SERVICE_URL);

        try
        {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("phonenumber", number));
            nameValuePairs.add(new BasicNameValuePair("message", message));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httpPost);

            return response;

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        return null;
    }
}