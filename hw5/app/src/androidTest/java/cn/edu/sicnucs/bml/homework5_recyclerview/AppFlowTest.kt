package cn.edu.sicnucs.bml.homework5_recyclerview

import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class AppFlowTest {
 @Test fun saveAndCancelProfile() {
  ActivityScenario.launch(MainActivity::class.java).use {
   it.onActivity { activity ->
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
   }
   onView(withId(R.id.btnEditProfile)).perform(click())
   onView(withId(R.id.et_name)).perform(replaceText("Alice"),closeSoftKeyboard())
   onView(withId(R.id.main)).perform(swipeUp())
   onView(withId(R.id.btnSave)).perform(click())
   onView(withId(R.id.tv_name)).check(matches(withText("姓名：Alice")))
   onView(withId(R.id.btnEditProfile)).perform(click())
   onView(withId(R.id.et_name)).perform(replaceText("Bob"),closeSoftKeyboard())
   onView(withId(R.id.main)).perform(swipeUp())
   onView(withId(R.id.btnCancel)).perform(click())
   onView(withId(R.id.tv_name)).check(matches(withText("姓名：Alice")))
   it.recreate()
   onView(withId(R.id.tv_name)).check(matches(withText("姓名：Alice")))
  }
 }
 @Test fun flipResetAndExit() {
  ActivityScenario.launch(MainActivity::class.java).use {
   it.onActivity { activity ->
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
   }
   onView(withId(R.id.main)).perform(swipeUp())
   onView(withId(R.id.btnStartGame)).perform(click())
   onView(withContentDescription("第1张牌，未翻开")).perform(click())
   onView(withId(R.id.tvScore)).check(matches(withText("得分：-1")))
   onView(withId(R.id.btnReset)).perform(click())
   onView(withId(R.id.tvScore)).check(matches(withText("得分：0")))
   onView(withContentDescription("第1张牌，未翻开")).check(matches(isDisplayed()))
   onView(withId(R.id.btnExit)).perform(click())
   onView(withId(R.id.btnEditProfile)).check(matches(isDisplayed()))
  }
 }
}
