package com.tealium.digitalvelocity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.tealium.digitalvelocity.data.Model;

import java.net.URLEncoder;


public final class ContactActivity extends DrawerLayoutActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        findViewById(R.id.contact_facebook).setOnClickListener(createFBClickListener());
        findViewById(R.id.contact_mail).setOnClickListener(createEmailClickListener());
        findViewById(R.id.contact_phone).setOnClickListener(createPhoneClickListener());
        findViewById(R.id.contact_twitter).setOnClickListener(createTwitterClickListener());
    }

    private View.OnClickListener createPhoneClickListener() {


        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String phoneNumber = Model.getInstance().getContactPhoneNumber();
                if (phoneNumber == null) {
                    Toast.makeText(v.getContext(), R.string.contact_no_phone, Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ContactActivity.this, R.string.contact_phone_error, Toast.LENGTH_SHORT).show();
                } catch (Throwable t) {
                    Toast.makeText(v.getContext(), R.string.contact_no_phone, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private View.OnClickListener createTwitterClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String hashTag = Model.getInstance().getContactTwitter();
                if (hashTag == null) {
                    Toast.makeText(v.getContext(), R.string.contact_no_twitter, Toast.LENGTH_SHORT).show();
                    return;
                }

                try {


                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://twitter.com/search?q=" + URLEncoder.encode("#" + hashTag, "utf-8")))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Throwable t) {
                    Toast.makeText(v.getContext(), R.string.contact_no_twitter, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private View.OnClickListener createEmailClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Model model = Model.getInstance();
                final String email = model.getContactEmail();
                final String emailHeader = model.getContactEmailHeader();
                final String emailMessage = model.getContactEmailMessage();
                if (TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(emailHeader)) {
                    Toast.makeText(v.getContext(), R.string.contact_no_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                //Uri data = Uri.parse("mailto:?subject=" + subject + "&body=" + body);

                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/html");
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                i.putExtra(Intent.EXTRA_SUBJECT, emailHeader);
                if (!TextUtils.isEmpty(emailMessage)) {
                    i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<p>" + emailMessage + "</p>"));
                }

                try {
                    startActivity(Intent.createChooser(i, "Email Digital Velocity"));
                } catch (Throwable t) {
                    Toast.makeText(v.getContext(), R.string.contact_no_email, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private View.OnClickListener createFBClickListener() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String facebookUri = Model.getInstance().getContactFacebook();
                if (facebookUri == null) {
                    Toast.makeText(v.getContext(), R.string.contact_no_facebook, Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUri))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(i);
                } catch (Throwable t) {
                    Toast.makeText(v.getContext(), R.string.contact_no_facebook, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}
