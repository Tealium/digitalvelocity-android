package com.tealium.digitalvelocity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.ModelTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DemoActivityTest {

    @Rule
    public ActivityTestRule<DemoActivity> mActivityRule = new ActivityTestRule<>(DemoActivity.class);

    @Before
    public void setUp() throws Exception {
        ModelTest.clearModel(Model.getInstance());
    }

    @Test
    public void testFieldEmptyPopulation() throws Exception {




        onView(withId(R.id.demo_account_input)).check(matches(withText("")));
        onView(withId(R.id.demo_profile_input)).check(matches(withText("")));
        onView(withId(R.id.demo_env_input)).check(matches(withText("")));
        onView(withId(R.id.demo_trace_input)).check(matches(withText("")));
    }

    @After
    public void tearDown() throws Exception {

    }
}