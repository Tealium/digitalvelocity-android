package com.tealium.digitalvelocity.data;

import android.app.Application;
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

    public void testIsFirstLaunchSinceUpdate() throws Exception {

        // No version code stored.
        Assert.assertTrue(Model.getInstance().isFirstLaunchSinceUpdate());


        // Version code should now be stored.
        Assert.assertFalse(Model.getInstance().isFirstLaunchSinceUpdate());
    }
}
