package com.tealium.digitalvelocity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tealium.digitalvelocity.data.IOUtils;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.gson.AgendaItem;
import com.tealium.digitalvelocity.event.LoadRequest;
import com.tealium.digitalvelocity.event.LoadedEvent;
import com.tealium.digitalvelocity.event.TrackEvent;
import com.tealium.digitalvelocity.util.Constant;
import com.tealium.digitalvelocity.util.Util;
import com.tealium.library.DataSources;

import java.io.File;

import de.greenrobot.event.EventBus;


public final class AgendaDetailActivity extends Activity {

    public static final String EXTRA_AGENDA_ITEM_ID = "agenda_item_id";
    public static final int RESULT_FAVORITE_TOGGLED = 2;

    private TextView faLabel;
    private ImageView imageView;
    private TextView titleLabel;
    private TextView subtitleLabel;
    private TextView timeLocLabel;
    private TextView descLabel;
    private CheckBox favoriteCheckBox;

    private String mTitle;
    private String mSubtitle;
    private String mObjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_agenda_detail);

        final String agendaItemId = getIntent().getStringExtra(EXTRA_AGENDA_ITEM_ID);

        if (Util.isEmptyOrNull(agendaItemId)) {
            Toast.makeText(
                    this,
                    "Uh-oh, something went wrong (no " + EXTRA_AGENDA_ITEM_ID + " was specified)",
                    Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }

        this.imageView = (ImageView) this.findViewById(R.id.agenda_detail_image);
        this.faLabel = (TextView) this.findViewById(R.id.agenda_detail_label_image);
        this.titleLabel = (TextView) this.findViewById(R.id.agenda_detail_label_title);
        this.subtitleLabel = (TextView) this.findViewById(R.id.agenda_detail_label_subtitle);
        this.timeLocLabel = (TextView) this.findViewById(R.id.agenda_detail_label_time_loc);
        this.descLabel = (TextView) this.findViewById(R.id.agenda_detail_label_desc);
        this.favoriteCheckBox = (CheckBox) this.findViewById(R.id.agenda_detail_toggle_favorite);

        final EventBus bus = EventBus.getDefault();
        bus.register(this);
        bus.post(new LoadRequest.AgendaItemData(agendaItemId));
    }

    @Override
    protected void onStop() {

        EventBus bus = EventBus.getDefault();
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_agenda_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.agenda_detail_menu_close) {
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        //this.overridePendingTransition(R.anim.hold, R.anim.out_to_bottom);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.AgendaItemData event) {
        EventBus.getDefault().unregister(this);

        AgendaItem item = event.getItem();

        mObjectId = item.getId();
        this.titleLabel.setText(mTitle = item.getTitle());
        this.subtitleLabel.setText(mSubtitle = item.getSubtitle());
        this.subtitleLabel.setVisibility(Util.isEmptyOrNull(item.getSubtitle()) ? View.GONE : View.VISIBLE);
        this.timeLocLabel.setText(item.getTimeLocDescription());
        this.descLabel.setText(item.getDescription());
        this.favoriteCheckBox.setChecked(Model.getInstance().isAgendaFavorite(item));
        this.favoriteCheckBox.setOnCheckedChangeListener(createCheckedChangeListener(item));

        final File file = IOUtils.getImageFile(this.imageView.getContext(), item.getId());
        final boolean hasFAValue = !Util.isEmptyOrNull(item.getFontAwesomeValue());
        final boolean imgExists = file != null;

        if (imgExists || !hasFAValue) {
            this.imageView.setVisibility(View.VISIBLE);
            this.faLabel.setVisibility(View.GONE);

            if (imgExists) {
                this.imageView.setImageURI(Uri.fromFile(file));
                if (BuildConfig.DEBUG) {
                    Log.d(Constant.TAG, "#Settimg image: " + file);
                }
            } else if (BuildConfig.DEBUG) {
                Log.d(Constant.TAG, "# No Image, no FA");
            }
        } else {
            this.imageView.setVisibility(View.GONE);
            this.faLabel.setVisibility(View.VISIBLE);

            this.faLabel.setText(Html.fromHtml("&#x" + item.getFontAwesomeValue() + ';'));
            if (BuildConfig.DEBUG) {
                Log.d(Constant.TAG, "# Setting FA to &#x" + item.getFontAwesomeValue() + ';');
            }
        }
    }

    private CompoundButton.OnCheckedChangeListener createCheckedChangeListener(final AgendaItem item) {

        return new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Model.getInstance().setAgendaFavorite(item, isChecked);

                EventBus.getDefault().post(TrackEvent.createLinkTrackEvent()
                        .add(DataSources.Key.LINK_ID, "agenda_favorite_toggled")
                        .add("agenda_objectid", mObjectId)
                        .add("agenda_title", mTitle)
                        .add("agenda_subtitle", mSubtitle)
                        .add("agenda_favorite", String.valueOf(isChecked)));

                if (isChecked) {
                    Toast.makeText(
                            AgendaDetailActivity.this,
                            R.string.agenda_detail_toast_favorite,
                            Toast.LENGTH_SHORT).show();
                }

                setResult(RESULT_FAVORITE_TOGGLED);
            }
        };
    }
}
