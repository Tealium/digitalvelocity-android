package com.tealium.digitalvelocity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tealium.beacon.event.BeaconEntered;
import com.tealium.beacon.event.BeaconExited;
import com.tealium.beacon.event.BeaconUpdate;
import com.tealium.digitalvelocity.data.IOUtils;
import com.tealium.digitalvelocity.util.Constant;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;


public final class DebugActivity extends Activity {

    private Controller controller;
    private BeaconAdapter beaconAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        ((Spinner) this.findViewById(R.id.debug_option_spinner))
                .setOnItemSelectedListener(createOptionListener());

        this.beaconAdapter = new BeaconAdapter();
        this.findViewById(R.id.debug_button_crash).setOnClickListener(createCrashClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus bus = EventBus.getDefault();
        if (!bus.isRegistered(this.beaconAdapter)) {
            bus.register(this.beaconAdapter);
        }

        if (this.controller != null) {
            this.controller.onSelection();
        }
    }

    @Override
    protected void onPause() {

        if (this.controller != null) {
            this.controller.onDeselection();
        }

        EventBus.getDefault().unregister(this.beaconAdapter);

        super.onPause();
    }

    private AdapterView.OnItemSelectedListener createOptionListener() {

        final Map<String, Controller> controllers = new HashMap<>(3);

        controllers.put(
                this.getString(R.string.debug_option_beacon_events),
                new BeaconController());

        controllers.put(
                this.getString(R.string.debug_option_files),
                new DirController(this.getFilesDir()));

        controllers.put(
                getString(R.string.debug_option_sp),
                new SPController());


        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Controller selectedController =
                        controllers.get(parent.getAdapter().getItem(position));

                if (selectedController == null) {
                    Toast.makeText(DebugActivity.this, "Unknown selection", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (controller != null) {
                    controller.onDeselection();
                }

                controller = selectedController;
                controller.onSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    private View.OnClickListener createCrashClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new RuntimeException("Test Crash");
            }
        };
    }

    private static interface Controller {

        public void onSelection();

        public void onDeselection();
    }

    private class BeaconController implements Controller {

        private final ExpandableListView expListView;

        BeaconController() {
            this.expListView = (ExpandableListView) findViewById(R.id.debug_content_exp_list);
        }

        @Override
        public void onSelection() {
            this.expListView.setVisibility(View.VISIBLE);
            this.expListView.setAdapter(beaconAdapter);
        }

        @Override
        public void onDeselection() {
            this.expListView.setVisibility(View.GONE);
        }
    }

    private class BeaconAdapter extends BaseExpandableListAdapter {

        private List<String> beaconEvents = new LinkedList<>();

        @Override
        public int getGroupCount() {
            return this.beaconEvents.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.beaconEvents.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            TextView label = (TextView) convertView;
            if (label == null) {
                label = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_debug_sp_group, parent, false);
            }

            label.setText(this.beaconEvents.get(groupPosition));

            return label;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(BeaconEntered event) {
            beaconEvents.add(">>> " + truncate(event.getId()) + "(" + event.getRssi() + ")");
            this.notifyDataSetChanged();
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(BeaconUpdate event) {
            beaconEvents.add("### " + truncate(event.getId()) + "(" + event.getRssi() + ")");
            this.notifyDataSetChanged();
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(BeaconExited event) {
            beaconEvents.add("<<< " + truncate(event.getId()) + "(" + event.getRssi() + ")");
            this.notifyDataSetChanged();
        }

        private String truncate(String id) {
            String[] comps = id.split("\\.");
            if (comps.length == 3) {
                return comps[1] + "." + comps[2];
            }

            return id;
        }

    }

    private class SPController implements Controller {

        private final ExpandableListView expListView;
        private final SPAdapter adapter;

        private SPController() {
            this.expListView = (ExpandableListView) findViewById(R.id.debug_content_exp_list);
            this.adapter = new SPAdapter();
        }

        @Override
        public void onSelection() {
            this.expListView.setAdapter(this.adapter);
            this.expListView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onDeselection() {
            this.expListView.setVisibility(View.GONE);
        }
    }

    private class SPAdapter extends BaseExpandableListAdapter {

        private final List<String> names;
        private final Map<String, List<Map.Entry<String, ?>>> data;

        private SPAdapter() {

            @SuppressLint("SdCardPath")
            File prefsDir = new File("/data/data/" + getPackageName() + "/shared_prefs");
            String[] names = prefsDir.list();
            Set<? extends Map.Entry<String, ?>> loadedSP;

            this.names = new ArrayList<>(names.length);
            this.data = new HashMap<>(names.length);

            for (String name : names) {
                if (name.endsWith(".xml")) {
                    name = name.substring(0, name.length() - 4);
                    this.names.add(name);
                    loadedSP = getSharedPreferences(name, 0).getAll().entrySet();
                    this.data.put(name, new ArrayList<>(loadedSP));
                }
            }
        }

        @Override
        public int getGroupCount() {
            return this.names.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this.getChildGroup(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.names.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return this.getChildGroup(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return (groupPosition * 1000) + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            TextView label = (TextView) convertView;

            if (label == null) {
                label = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_debug_sp_group, parent, false);
            }

            label.setText(this.names.get(groupPosition));

            return label;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView label = (TextView) convertView;

            if (label == null) {
                label = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_debug_sp_child, parent, false);
            }

            Map.Entry<String, ?> entry = this.getChildGroup(groupPosition).get(childPosition);
            label.setText(entry.getKey() + "=" + entry.getValue());


            return label;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        private List<Map.Entry<String, ?>> getChildGroup(int groupPosition) {
            final String name = this.names.get(groupPosition);
            return this.data.get(name);
        }
    }

    private class DirController implements Controller, DialogInterface.OnClickListener, ExpandableListView.OnChildClickListener {

        private final ExpandableListView expListView;
        private final DirAdapter adapter;
        private File selectedFile;

        private DirController(File file) {
            this.expListView = (ExpandableListView) findViewById(R.id.debug_content_exp_list);
            this.adapter = new DirAdapter(file.listFiles());
        }

        @Override
        public void onSelection() {
            this.expListView.setAdapter(this.adapter);
            this.expListView.setVisibility(View.VISIBLE);
            this.expListView.setOnChildClickListener(this);
        }

        @Override
        public void onDeselection() {
            this.expListView.setVisibility(View.GONE);
            this.expListView.setOnChildClickListener(null);
            this.selectedFile = null;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {


            if (which != DialogInterface.BUTTON_POSITIVE) {
                return;
            }

            Log.d(Constant.TAG, "Deleting " + this.selectedFile);

            File renamed = new File(
                    this.selectedFile.getParentFile(),
                    System.currentTimeMillis() + this.selectedFile.getName());
            this.selectedFile.renameTo(renamed);
            this.selectedFile = null;
            renamed.delete();
            this.adapter.notifyDataSetChanged();

        }

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            this.selectedFile = (File) adapter.getGroup(groupPosition);

            new AlertDialog.Builder(parent.getContext())
                    .setTitle("Delete?")
                    .setPositiveButton("Yes", this)
                    .setNegativeButton("No", this)
                    .create().show();

            return true;
        }
    }

    private class DirAdapter extends BaseExpandableListAdapter {

        private final String[] SUFFIXES = {
                IOUtils.SUFFIX_SPONSOR,
                IOUtils.SUFFIX_FLOOR,
                IOUtils.SUFFIX_NOTIFICATION,
                IOUtils.SUFFIX_COORDINATES,
                IOUtils.SUFFIX_AGENDA_ITEM
        };

        private final File[] files;

        private DirAdapter(File[] files) {
            this.files = files;
        }

        @Override
        public int getGroupCount() {
            return this.files.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.files[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            try {
                return IOUtils.readFile(this.files[groupPosition]);
            } catch (IOException e) {
                Log.e(Constant.TAG, "Error reading " + this.files[groupPosition].getAbsolutePath(), e);
            }


            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return (groupPosition * 1000) + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            TextView label = (TextView) convertView;

            if (label == null) {
                label = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_debug_sp_group, parent, false);
            }

            label.setText(this.files[groupPosition].getName());

            return label;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            File file = this.files[groupPosition];
            final String name = file.getName();

            if (name.endsWith(".png") ||
                    name.endsWith(".jpg") ||
                    name.endsWith(".jpeg") ||
                    name.endsWith(".gif")) {
                return this.getImageView(file, convertView, parent);
            }

            for (String suffix : SUFFIXES) {
                if (name.endsWith(suffix)) {
                    return this.getJSONView(file, convertView, parent);
                }
            }

            if (name.endsWith(".log")) {
                TextView label = getDefaultView(convertView, parent);
                try {
                    final String log = IOUtils.readFile(file);
                    label.setText(log);
                } catch (IOException e) {
                    label.setText(e.toString());
                }
                return label;
            }

            return this.getDefaultView(convertView, parent);
        }

        private View getJSONView(File file, View convertView, ViewGroup parent) {

            TextView label = this.getDefaultView(convertView, parent);

            try {
                label.setText(new JSONObject(IOUtils.readFile(file)).toString(4));
            } catch (Throwable t) {
                label.setText(t.toString());
            }

            return label;
        }

        private View getImageView(File file, View convertView, ViewGroup parent) {
            ImageView imgView;

            if (convertView == null || !(convertView instanceof ImageView)) {
                imgView = (ImageView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_debug_image, parent, false);
            } else {
                imgView = (ImageView) convertView;
            }

            imgView.setImageURI(Uri.fromFile(file));

            return imgView;
        }

        private TextView getDefaultView(View convertView, ViewGroup parent) {

            TextView label;

            if (convertView == null || !(convertView instanceof TextView)) {
                label = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_debug_sp_child, parent, false);
            } else {
                label = (TextView) convertView;
            }

            label.setText("???");
            return label;
        }


        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
