package com.tealium.digitalvelocity.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.test.ApplicationTestCase;

import com.tealium.digitalvelocity.util.Constant;

import junit.framework.Assert;

public class ModelTest extends ApplicationTestCase<Application> {

    public ModelTest() {
        super(Application.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.createApplication();

        // Reset shared preferences.
        this.getApplication().getSharedPreferences(Constant.SP.NAME, 0)
                .edit().clear().commit();

        Model.setup(this.getApplication());
    }

    public void testSetOrRemoveIfNull() throws Exception {

        final String KEY = "foo";
        final String VAL = "bar";
        final SharedPreferences sp = getApplication().getSharedPreferences("test", 0);
        sp.edit().clear().commit();

        SharedPreferences.Editor editor;

        // Determine that null does not set
        Model.Test.setOrRemoveIfNull(editor = sp.edit(), KEY, null);
        editor.commit();

        Assert.assertFalse(sp.contains(KEY));

        // Determine that empty-string does not set
        Model.Test.setOrRemoveIfNull(editor = sp.edit(), KEY, "");
        editor.commit();

        Assert.assertFalse(sp.contains(KEY));

        // Determine key is settable
        Model.Test.setOrRemoveIfNull(editor = sp.edit(), KEY, VAL);
        editor.commit();

        Assert.assertEquals(VAL, sp.getString(KEY, null));

        // Determine that null removes key
        Model.Test.setOrRemoveIfNull(editor = sp.edit(), KEY, null);
        editor.commit();

        Assert.assertFalse(sp.contains(KEY));

        // Determine that empty string removes key
        Model.Test.setOrRemoveIfNull(editor = sp.edit(), KEY, VAL);
        Model.Test.setOrRemoveIfNull(sp.edit(), KEY, "");
        editor.commit();

        Assert.assertFalse(sp.contains(KEY));
    }

    public void testIsFirstLaunchSinceUpdate() throws Exception {

        // No version code stored.
        Assert.assertTrue(Model.getInstance().isFirstLaunchSinceUpdate());


        // Version code should now be stored.
        Assert.assertFalse(Model.getInstance().isFirstLaunchSinceUpdate());
    }

    public static void clearModel(Model model) {
        Model.Test.clear(model);
    }
}
