package com.tealium.digitalvelocity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.SystemRequirementsHelper;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.drawerlayout.DrawerAdapter;
import com.tealium.digitalvelocity.drawerlayout.DrawerItem;
import com.tealium.digitalvelocity.view.Dialogs;

public class DrawerLayoutActivity extends Activity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    // 5 minutes
    private static final long ASK_LOCATION_TIMEOUT = 5L * 60L * 1000L;

    private AlertDialog mBluetoothPrompt;
    private DrawerLayout mDrawerLayout;
    private View mDrawer;
    private TextView mTitleLabel;
    private boolean mFilterFavorites;
    // for all instances
    private static long mLastTimeLocationRequested;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_drawerlayout);

        LayoutInflater.from(this).inflate(
                layoutResID,
                (ViewGroup) findViewById(R.id.drawerlayout_content));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_root);
        mDrawer = mDrawerLayout.findViewById(R.id.drawerlayout_drawer_right);

        ListView menu = (ListView) mDrawer.findViewById(R.id.drawerlayout_drawer_menu);
        menu.setAdapter(new DrawerAdapter(this));
        menu.setOnItemClickListener(createNavigationClickListener());

        findViewById(R.id.drawerlayout_drawer_button_settings)
                .setOnClickListener(createSettingsClickListener());

        setupActionBar();
    }

    @SuppressLint("NewApi") // Too stupid to realize that the API won't be used in pre-M
    @Override
    protected void onResume() {
        super.onResume();

        final boolean isShowingLoginScreen = Model.getInstance().getUserEmail() == null;
        final long now = SystemClock.elapsedRealtime();

        if (isShowingLoginScreen || (now - mLastTimeLocationRequested) < ASK_LOCATION_TIMEOUT) {
            return;
        }

        mLastTimeLocationRequested = now;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (SystemRequirementsHelper.isBluetoothLeAvailable(this) &&
                    !SystemRequirementsHelper.isBluetoothEnabled(this) &&
                    Model.getInstance().canPromptBluetooth()) {
                (mBluetoothPrompt = Dialogs.createBluetoothPrompt(this)).show();
            }
            return;
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

        } else if (SystemRequirementsHelper.isBluetoothLeAvailable(this) &&
                !SystemRequirementsHelper.isBluetoothEnabled(this)) {
            // TODO Externalize
            Toast.makeText(
                    this,
                    "Using bluetooth for location services",
                    Toast.LENGTH_LONG).show();
            BluetoothAdapter.getDefaultAdapter().enable();

            if (!SystemRequirementsHelper.checkAllPermissions(this)) {
                Toast.makeText(
                        this,
                        "Please enable Location",
                        Toast.LENGTH_LONG).show();
            }
        } else if (!SystemRequirementsHelper.checkAllPermissions(this)) {
            Toast.makeText(
                    this,
                    "Please enable Location",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        if (mBluetoothPrompt != null) {
            mBluetoothPrompt.dismiss();
        }
        super.onPause();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != REQUEST_LOCATION_PERMISSION) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // TODO Externalize
            Toast.makeText(this, "Using bluetooth for location services", Toast.LENGTH_LONG).show();
            BluetoothAdapter.getDefaultAdapter().enable();
            if (!SystemRequirementsHelper.checkAllPermissions(this)) {
                Toast.makeText(
                        this,
                        "Please enable Location",
                        Toast.LENGTH_LONG).show();
            }
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // TODO Externalize
            Toast.makeText(this, "Enable location to gain access to exclusive experiences at Digital Velocity.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        if (mDrawerLayout.isDrawerOpen(mDrawer)) {
            mDrawerLayout.closeDrawer(mDrawer);
        }

        super.onStop();
    }

    @Override
    public void setTitle(CharSequence title) {
        if (mTitleLabel != null) {
            mTitleLabel.setText(title);
        } else {
            super.setTitle(title);
        }
    }

    private void setupActionBar() {

        findViewById(R.id.actionbar_imageview_hamburger)
                .setOnClickListener(createHamburgerClickListener());

        if (isShowingLogo()) {
            findViewById(R.id.actionbar_title).setVisibility(View.GONE);
            findViewById(R.id.actionbar_image_logo).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.actionbar_title).setVisibility(View.VISIBLE);
            findViewById(R.id.actionbar_image_logo).setVisibility(View.GONE);
            mTitleLabel = ((TextView) findViewById(R.id.actionbar_title));
            mTitleLabel.setText(getTitle());
        }

        final View favoritesImageView = findViewById(R.id.actionbar_imageview_favorites);
        if (isFilteringFavorites()) {
            favoritesImageView.setVisibility(View.VISIBLE);
            favoritesImageView.setOnClickListener(createFilterClickListener());
        } else {
            favoritesImageView.setVisibility(View.GONE);
        }
    }

    /**
     * Show logo instead of title
     */
    protected boolean isShowingLogo() {
        return false;
    }

    protected boolean isFilteringFavorites() {
        return false;
    }

    protected void onFilterClick(boolean shouldFilterFavorites) {

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
                    // Ensure in bounds
                    return;
                }

                Class<? extends Activity> targetClass =
                        items[position].getActivityClass();

                if (DrawerLayoutActivity.this.getClass().equals(targetClass)) {
                    return;
                }

                Intent i = new Intent(DrawerLayoutActivity.this, targetClass);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        };
    }

    private View.OnClickListener createHamburgerClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(mDrawer)) {
                    mDrawerLayout.closeDrawer(mDrawer);
                } else {
                    mDrawerLayout.openDrawer(mDrawer);
                }
            }
        };
    }

    private View.OnClickListener createFilterClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterFavorites = !mFilterFavorites;
                ((ImageView) v).setImageResource(mFilterFavorites ?
                        android.R.drawable.star_big_on : android.R.drawable.star_big_off);

                onFilterClick(mFilterFavorites);
            }
        };
    }
}
