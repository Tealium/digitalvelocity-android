package com.tealium.digitalvelocity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public final class ContactActivity extends DrawerLayoutActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        this.findViewById(R.id.contact_facebook).setOnClickListener(createFBClickListener());
        this.findViewById(R.id.contact_mail).setOnClickListener(createEmailClickListener());
        this.findViewById(R.id.contact_phone).setOnClickListener(createPhoneClickListener());
        this.findViewById(R.id.contact_twitter).setOnClickListener(createTwitterClickListener());
    }

    private View.OnClickListener createPhoneClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse("tel:+44 (0) 20 70 84 62 68"));
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ContactActivity.this, R.string.contact_phone_error, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private View.OnClickListener createTwitterClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/hashtag/dveu"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }

    private View.OnClickListener createEmailClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Uri data = Uri.parse("mailto:?subject=" + subject + "&body=" + body);

                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/html");
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"digitalvelocity@tealium.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Digital Velocity Question");
                //i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<b>this is html text in email body.</b>"));
                startActivity(Intent.createChooser(i, "Email Digital Velocity"));
            }
        };
    }

    private View.OnClickListener createFBClickListener() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/events/1078267485522323/"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(i);
            }
        };
    }
}
