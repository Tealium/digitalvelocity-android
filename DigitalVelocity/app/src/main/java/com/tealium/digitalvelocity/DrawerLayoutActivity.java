package com.tealium.digitalvelocity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tealium.digitalvelocity.drawerlayout.DrawerAdapter;
import com.tealium.digitalvelocity.drawerlayout.DrawerItem;
import com.tealium.digitalvelocity.util.Constant;

public class DrawerLayoutActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private View drawer;
    private TextView titleLabel;
    private boolean hasLogo;
    private boolean isFilteringFavorites;
    private MenuItem filterMenuItem;
    private boolean filterFavorites;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_drawerlayout);

        this.setupManifestOptions();

        LayoutInflater.from(this).inflate(
                layoutResID,
                (ViewGroup) this.findViewById(R.id.drawerlayout_content));

        this.drawerLayout = (DrawerLayout) this.findViewById(R.id.drawerlayout_root);
        this.drawer = this.drawerLayout.findViewById(R.id.drawerlayout_drawer_right);

        ListView menu = (ListView) this.drawer.findViewById(R.id.drawerlayout_drawer_menu);
        menu.setAdapter(new DrawerAdapter(this));
        menu.setOnItemClickListener(createNavigationClickListener());

        this.findViewById(R.id.drawerlayout_drawer_button_settings)
                .setOnClickListener(createSettingsClickListener());

        this.setupActionBar();
    }

    @Override
    protected void onStop() {
        if (this.drawerLayout.isDrawerOpen(this.drawer)) {
            this.drawerLayout.closeDrawer(this.drawer);
        }

        super.onStop();
    }

    private void setupManifestOptions() {
        try {
            ActivityInfo info = getPackageManager().getActivityInfo(
                    this.getComponentName(),
                    PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);

            if (info.metaData == null) {
                this.hasLogo = false;
                this.isFilteringFavorites = false;
                return;
            }

            this.hasLogo = info.metaData.getBoolean("com.tealium.digitalvelocity.activity.show_logo", false);
            this.isFilteringFavorites = info.metaData.getBoolean("com.tealium.digitalvelocity.activity.show_filter", false);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constant.TAG, null, e);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (this.titleLabel != null) {
            this.titleLabel.setText(title);
        } else {
            super.setTitle(title);
        }
    }

    private void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(4, 4, 4)));

        if (this.hasLogo) {
            actionBar.setCustomView(R.layout.actionbar_logo);
            return;
        }

        actionBar.setCustomView(R.layout.actionbar_titled);

        this.titleLabel = ((TextView) actionBar.getCustomView().findViewById(R.id.actionbar_title));
        this.titleLabel.setText(this.getTitle());
        if (AgendaActivity.class.equals(this.getClass())) {
            final int paddingLeft = this.getResources()
                    .getDimensionPixelSize(R.dimen.action_bar_title_padding_left);
            this.titleLabel.setPadding(4 * paddingLeft, 0, 0, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drawerlayout, menu);

        this.filterMenuItem = menu.findItem(R.id.menu_drawerlayout_filter_favorites);
        this.filterMenuItem.setVisible(this.isFilteringFavorites);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_drawerlayout_hamburger:
                if (this.drawerLayout.isDrawerOpen(this.drawer)) {
                    this.drawerLayout.closeDrawer(this.drawer);
                } else {
                    this.drawerLayout.openDrawer(this.drawer);
                }
                return true;
            case R.id.menu_drawerlayout_filter_favorites:

                this.filterFavorites = !this.filterFavorites;

                this.filterMenuItem.setIcon(this.filterFavorites ?
                        android.R.drawable.star_big_on : android.R.drawable.star_big_off);

                this.onFilterClick(this.filterFavorites);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onFilterClick(boolean filterFavorites) {

    }

    private View.OnClickListener createSettingsClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (DrawerLayoutActivity.this.getClass().equals(SettingsActivity.class)) {
                    return;
                }

                Intent i = new Intent(DrawerLayoutActivity.this, SettingsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        };
    }

    private AdapterView.OnItemClickListener createNavigationClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final DrawerItem[] items = DrawerItem.values();

                if (position >= items.length) {
                    return;
                }

                if (DrawerItem.CHAT.equals(items[position])) {
                    //if (!DonkyManager.startChat(DrawerLayoutActivity.this)) {
                    // TODO: localize
                    Toast.makeText(
                            DrawerLayoutActivity.this,
                            "Chat is not available at this time",
                            Toast.LENGTH_SHORT).show();
                    //}
                    return;
                }

                Class<? extends Activity> targetClass =
                        items[position].getActivityClass();

                if (DrawerLayoutActivity.this.getClass().equals(targetClass)) {
                    return;
                }

                /*
                int inAnimation = LocationActivity.class.equals(targetClass) ?
                        R.anim.in_from_bottom : R.anim.in_from_left;
                */
                Intent i = new Intent(DrawerLayoutActivity.this, targetClass);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        };
    }
}
