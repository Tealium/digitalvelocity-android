package com.tealium.debug;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

public final class Util {
    private Util() {
    }

    /**
     * Stringifies a view hierarchy of an Activity.
     */
    public static String describe(Activity activity) {

        if (activity == null) {
            return null;
        }

        return describe(activity.getWindow().getDecorView().getRootView());
    }

    /**
     * Stringifies a view hierarchy of a View.
     */
    public static String describe(View view) {
        return describe(view, "", view.getResources());
    }

    public static String describeObject(Object o) {
        if (o == null) {
            return null;
        }

        if (o.getClass().isArray()) {
            String s = "[\n";

            final int length = Array.getLength(o);

            for (int i = 0; i < length; i++) {
                s += "    " + Array.get(o, i) + ((i == length - 1) ? "," : "") + "\n";
            }

            return s + "]";
        } else {
            return "TODO"; // TODO
        }
    }


    public static String describe(List<?> list) {

        if (list == null) {
            return null;
        }

        String s = "[\n";

        for (Iterator<?> i = list.iterator(); i.hasNext(); ) {
            s += "    " + i.next() + (i.hasNext() ? "," : "") + "\n";
        }

        return s + "]";
    }

    public static String describe(View view, String indent, Resources res) {

        if (res == null) {
            throw new IllegalArgumentException();
        }

        String s = indent + "<" + view.getClass().getName();

        if (view.getId() != View.NO_ID) {
            s += " id=\"" + res.getResourceEntryName(view.getId()) + "\"";
        }

        if (view instanceof ViewGroup) {

            ViewGroup group = (ViewGroup) view;

            if (group.getChildCount() == 0) {
                return s + "/>";
            }

            s += ">\n";

            final String newIndent = indent + "|";

            for (int i = 0; i < group.getChildCount(); i++) {
                s += describe(group.getChildAt(i), newIndent, res) + "\n";
            }

            return s + indent + "</" + view.getClass().getName() + ">";
        }

        return s + "/>";
    }
}
