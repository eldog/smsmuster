package com.scraperwiki.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SMSMuster extends Activity
{
    private Button sendTestTextButton;

    private OnClickListener sendTestTextButtonListener = new OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.putExtra("sms_body", "This is a test text");
            sendIntent.setType("vnd.android-dir/mms-sms");
            startActivity(sendIntent);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sendTestTextButton = (Button) findViewById(R.id.sendTestText);
        sendTestTextButton.setOnClickListener(sendTestTextButtonListener);
    }
}